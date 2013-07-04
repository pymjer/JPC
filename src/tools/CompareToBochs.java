package tools;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class CompareToBochs
{
    public static String[] names = new String[]
        {
            "eax", "ecx", "edx", "ebx", "esp", "ebp", "esi", "edi","eip", "flags",
            /*10*/"es", "cs", "ss", "ds", "fs", "gs", "ticks",
            /*17*/"es-lim", "cs-lim", "ss-lim", "ds-lim", "fs-lim", "gs-lim", "cs-prop",
            /*24*/"gdtrbase", "gdtr-lim", "idtrbase", "idtr-lim", "ldtrbase", "ldtr-lim",
            /*30*/"es-base", "cs-base", "ss-base", "ds-base", "fs-base", "gs-base",
            /*36*/"cr0",
            /*37*/"ST0H", "ST0L","ST1H", "ST1L","ST2H", "ST2L","ST3H", "ST3L",
            /*45*/"ST4H", "ST4L","ST5H", "ST5L","ST6H", "ST6L","ST7H", "ST7L",
            //"expiry"
        };
    static String newJar = "JPCApplication.jar";
    public static final boolean compareFlags = true;
    public static final boolean compareStack = false;
    public static final boolean compareCMOS = false;
    public static final boolean comparePIT = true;
    public static final String[] perf = {"-fda", "floppy.img", "-boot", "fda", "-hda", "dir:dos"};

    public static final String[] doom = {"-fda", "floppy.img", "-boot", "fda", "-hda", "../../tmpdrives/doom10m.img"};
    public static final String[] doom2 = {"-fda", "floppy.img", "-boot", "fda", "-hda", "../../tmpdrives/doom2.img"};
    public static final String[] prince1 = {"-fda", "floppy.img", "-boot", "fda", "-hda", "../../tmpdrives/prince1.img"};
    public static final String[] pascalcrash = {"-fda", "floppy.img", "-boot", "fda", "-hda", "tests/CRASHES.img"};
    public static final String[] worms = {"-fda", "floppy.img", "-boot", "fda", "-hda", "worms.img"};
    public static final String[] war2 = {"-fda", "floppy.img", "-boot", "fda", "-hda", "war2demo.img"};
    public static final String[] linux = {"-hda", "../../tmpdrives/linux.img", "-boot", "hda"};
    public static final String[] bsd = {"-hda", "../../tmpdrives/netbsd.img", "-boot", "hda"};
    public static final String[] mosa = {"-hda", "mosa-project.img", "-boot", "hda"};
    public static final String[] dsl = {"-hda", "dsl-desktop-demo2.img", "-boot", "hda"};
    public static final String[] isolinux = {"-cdrom", "isolinux.iso", "-boot", "cdrom"};
    public static final String[] dslCD = {"-cdrom", "../../tmpdrives/dsl-n-01RC4.iso", "-boot", "cdrom"};
    public static final String[] hurd = {"-cdrom", "hurd.iso", "-boot", "cdrom"};
    public static final String[] tty = {"-cdrom", "ttylinux-i386-5.3.iso", "-boot", "cdrom"};
    public static final String[] win311 = {"-hda", "../../tmpdrives/win311.img", "-boot", "hda"};

    public static String[] pcargs = prince1;

    public static final int flagMask = ~0x000; // OF IF
    public static final int flagAdoptMask = ~0x10; // OF AF
    public static long startTime = System.nanoTime();
    public static int nextMilestone = 0;

    public static List<Integer> ignoredIOPorts = new ArrayList<Integer>();
    static{
        ignoredIOPorts.add(0x60); // keyboard
        ignoredIOPorts.add(0x64); // keyboard
        ignoredIOPorts.add(0x61); // PC speaker
    }

    public final static Map<String, Integer> flagIgnores = new HashMap();
    static
    {
        flagIgnores.put("test", ~0x10); // not defined in spec
        flagIgnores.put("and", ~0x10); // not defined in spec
        flagIgnores.put("sar", ~0x10); // not defined in spec for non zero shifts
        flagIgnores.put("xor", ~0x10); // not defined in spec
        flagIgnores.put("or", ~0x10); // not defined in spec
        flagIgnores.put("mul", ~0xd4); // not defined in spec
        flagIgnores.put("imul", ~0xd4); // not defined in spec
        flagIgnores.put("popfw", ~0x895);
        //flagIgnores.put("shl", ~0x810);
        //flagIgnores.put("bt", ~0x894);

        // not sure
        //flagIgnores.put("bts", ~0x1);

        // errors with the old JPC
        //flagIgnores.put("add", ~0x800)
        //flagIgnores.put("btr", ~0x1);
        flagIgnores.put("rcl", ~0x800);
        flagIgnores.put("shr", ~0x810);
        //flagIgnores.put("shrd", ~0x810);
        flagIgnores.put("shld", ~0x810);
        flagIgnores.put("lss", ~0x200);
        //flagIgnores.put("iret", ~0x10); // who cares about before the interrupt
        //flagIgnores.put("iretw", ~0x810); // who cares about before the interrupt

    }

    public static TreeSet<KeyBoardEvent> keyPresses = new TreeSet<KeyBoardEvent>();
    public static TreeSet<KeyBoardEvent> keyReleases = new TreeSet<KeyBoardEvent>();
    public static final long START_KEYS = 380000000L;
    static
    {
        //keyboardInput.add(new KeyBoardEvent(0x2000000L, "cd windows\n"));
        //keyboardInput.add(new KeyBoardEvent(0x2000100L, "win\n"));
        //keyboardInput.add(new KeyBoardEvent(0x7000000L, "./test-i386\n"));
        // prince
//        keyPresses.add(new KeyBoardEvent(200000000L, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(250000000L, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 10, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 20, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 30, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 40, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 50, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 60, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 70, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 80, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 90, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 100, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
//        keyPresses.add(new KeyBoardEvent(START_KEYS + 110, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
    }

    public static TreeSet<MouseEvent> mouseInput = new TreeSet<MouseEvent>();
    static
    {
        //mouseInput.add(new MouseEvent(0x42bb000L, 0, 0, 0, true, false, false));
        //mouseInput.add(new MouseEvent(0x42bb010L, 0, 0, 0, false, false, false));
        //mouseInput.add(new MouseEvent(0x6000000L, 0, 0, 0, false, false, true));
        //mouseInput.add(new MouseEvent(0x6000100L, 0, 0, 0, false, false, false));
    }

    public static void main(String[] args) throws Exception
    {
        boolean mem = false;
        if ((args.length >0) && args[0].equals("-mem"))
            mem = true;
        URL[] urls1 = new URL[]{new File(newJar).toURL()};
        ClassLoader cl1 = new URLClassLoader(urls1, Comparison.class.getClassLoader());

        Class opts = cl1.loadClass("org.jpc.j2se.Option");
        Method parse = opts.getMethod("parse", String[].class);
        parse.invoke(opts, (Object)args);

        Calendar start1 = Calendar.getInstance();
        start1.setTimeInMillis(1370072774000L); // hard coded into bochssrc

        Class c1 = cl1.loadClass("org.jpc.emulator.PC");
        Constructor ctor = c1.getConstructor(String[].class, Calendar.class);
        Object newpc = ctor.newInstance((Object)pcargs, start1);

        EmulatorControl bochs = new Bochs();

        Method m1 = c1.getMethod("hello");
        m1.invoke(newpc);

        Method ints1 = c1.getMethod("checkInterrupts");
        Method state1 = c1.getMethod("getState");
        Method cmos1 = c1.getMethod("getCMOS");
        Method pit1 = c1.getMethod("getPit");
        Method enterInt = c1.getMethod("forceEnterInterrupt", Integer.class);
        Method exitInt = c1.getMethod("forceExitInterrupt");
        Method ticksDelta = c1.getMethod("addToTicks", Integer.class);

        Method setState1 = c1.getMethod("setState", int[].class);
        Method execute1 = c1.getMethod("executeBlock");
        Method dirty1 = c1.getMethod("getDirtyPages", Set.class);
        Method save1 = c1.getMethod("savePage", Integer.class, byte[].class, Boolean.class);
        Method load1 = c1.getMethod("loadPage", Integer.class, byte[].class, Boolean.class);
        Method startClock1 = c1.getMethod("start");
        startClock1.invoke(newpc);
        Method break1 = c1.getMethod("eipBreak", Integer.class);
        Method instructionInfo = c1.getMethod("getInstructionInfo", Integer.class);

        Method keysDown1 = c1.getMethod("sendKeysDown", String.class);
        Method keysUp1 = c1.getMethod("sendKeysUp", String.class);
        Method minput1 = c1.getMethod("sendMouse", Integer.class, Integer.class, Integer.class, Integer.class);

        // setup screen from new JPC
        JPanel screen = (JPanel)c1.getMethod("getNewMonitor").invoke(newpc);
        JFrame frame = new JFrame();
        frame.getContentPane().add("Center", new JScrollPane(screen));
        frame.validate();
        frame.setVisible(true);
        frame.setBounds(100, 100, 760, 500);

        if (mem)
            System.out.println("Comparing memory"+(compareStack?", stack":"")+" and registers..");
        else if (compareStack)
            System.out.println("Comparing registers and stack..");
        else
            System.out.println("Comparing registers only..");
        String line;
        byte[] sdata1 = new byte[4096];
        byte[] sdata2 = new byte[4096];
        int[] fast = null, bochsState=null;
        boolean previousLss = false;
        int previousStackAddr = 0;
        boolean startedRight = false, startedLeft = false;
        int lastPIT0Count = 0;
        while (true)
        {
            Exception e1 = null;
            try {
                // check ints first to mirror bochs' behaviour of checking for an interrupt prior to execution
                ints1.invoke(newpc);
                execute1.invoke(newpc);
            } catch (Exception e)
            {
                printHistory();
                e.printStackTrace();
                System.out.println("Exception during new JPC execution... look above");
                e1 = e;
            }
            String nextBochs = null;
            try {
                nextBochs = bochs.executeInstruction();
            } catch (Exception e)
            {
                printHistory();
                e.printStackTrace();
                System.out.println("Exception during Bochs execution... look above");
                throw e;
            }
            fast = (int[])state1.invoke(newpc);
            bochsState = bochs.getState();
            try {
                line = (String) instructionInfo.invoke(newpc, new Integer(1)) + " == " + nextBochs; // instructions per block
            } catch (Exception e)
            {
                if (!e.toString().contains("PAGE_FAULT"))
                {
                    e.printStackTrace();
                    System.out.printf("Error getting instruction info.. at cs:eip = %08x\n", fast[8]+(fast[10]<<4));
                    line = "Instruction decode error";
                    printHistory();
                    //continueExecution("after Invalid decode at cs:eip");
                }
                line = "PAGE_FAULT getting instruction" + " == " + nextBochs;
            }
            if (e1 != null)
                throw e1;
            // account for repeated strings
            if ((fast[8] != bochsState[8]) && (currentInstruction().contains("rep")))
            {
                while (fast[8] != bochsState[8])
                {
                    bochs.executeInstruction();
                    bochsState = bochs.getState();
                }
                // now update ticks
                fast[16] = bochsState[16];
                setState1.invoke(newpc, (int[])fast);
            }
            // sometimes JPC does 2 instructions at once for atomicity relative to interrupts
            if (fast[16] == bochsState[16] +1)
            {
                try {
                    bochs.executeInstruction();
                    bochsState = bochs.getState();
                } catch (Exception e)
                {
                    printHistory();
                    e.printStackTrace();
                    System.out.println("Exception during Bochs execution... look above");
                    throw e;
                }
            }
            // send input events
            if (fast[16] > 230000000)
            {
                if (fast[16] < 240000000)
                {
                    if (!startedRight)
                    {
                        startedRight = true;
                        System.out.println("Started running right...");
                    }
                    keysDown1.invoke(newpc, ">");
                    bochs.keysDown(">");
                }
                else
                {
                    if (!startedLeft)
                    {
                        startedLeft = true;
                        System.out.println("Started running Left...");
                        keysUp1.invoke(newpc, ">");
                        bochs.keysUp(">");
                    }
                    keysDown1.invoke(newpc, "<");
                    bochs.keysDown("<");
                }
            }
            if (!keyPresses.isEmpty())
            {
                KeyBoardEvent k = keyPresses.first();
                if (fast[16] > k.time)
                {
                    keysDown1.invoke(newpc, k.text);
                    bochs.keysDown(k.text);
                    System.out.println("Sent key presses: "+k.text);
                    keyPresses.remove(k);
                }
            }
            if (!keyReleases.isEmpty())
            {
                KeyBoardEvent k = keyReleases.first();
                if (fast[16] > k.time)
                {
                    keysUp1.invoke(newpc, k.text);
                    bochs.keysUp(k.text);
                    System.out.println("Sent key releases: "+k.text);
                    keyReleases.remove(k);
                }
            }
            if (!mouseInput.isEmpty())
            {
                MouseEvent k = mouseInput.first();
                if (fast[16] > k.time)
                {
                    minput1.invoke(newpc, k.dx, k.dy, k.dz, k.buttons);
                    bochs.sendMouse(k.dx, k.dy, k.dz, k.buttons);
                    mouseInput.remove(k);
                }
            }
            if (fast[16] > nextMilestone)
            {
                System.out.printf("Reached %x ticks! Averaging %d IPS\n", fast[16], fast[16]*(long)1000000000/(System.nanoTime()-startTime));
                nextMilestone += 0x10000;
            }
            if (history[historyIndex] == null)
                history[historyIndex] = new Object[3];
            history[historyIndex][0] = fast;
            history[historyIndex][1] = bochsState;
            history[historyIndex][2] = line;
            historyIndex = (historyIndex+1)%history.length;
            
            // COMPARE INTERRUPT STATE
            int prevIndex = ((historyIndex-2)%history.length + history.length) % history.length;
            if (history[prevIndex] != null)
            {
                boolean JPCInt = inInt((int[])(history[prevIndex][0]), fast);
                boolean BochsInt = inInt((int[])(history[prevIndex][1]), bochsState);
                if (!BochsInt)
                {
                    if (JPCInt)
                    {
                        exitInt.invoke(newpc);
                        fast = (int[])state1.invoke(newpc);
                        System.out.println("Forced exit interrupt.");
                    }
                }
                else
                {
                    if (!JPCInt)
                    {
                        // need to make sure out eip is the correct one (bochs sometimes does the interrupt before the instruction)
                        int ssBase = fast[35];
                        int esp = fast[4] + ssBase;
                        boolean pm = (fast[36] & 1) != 0;
                        int espPageIndex;
                        if (pm)
                            espPageIndex = esp;
                        else
                            espPageIndex = esp >>> 12;
                        bochs.savePage(new Integer(espPageIndex), sdata2, pm);
                        int eip = (sdata2[esp & 0xfff] & 0xff) | ((sdata2[(esp+1) & 0xfff]& 0xff) << 8); // eip is the last thing pushed onto the stack
                        fast[8] = eip;
                        setState1.invoke(newpc, fast); // hope the instruction didn't have side effects
                        // now cause the interrupt
                        enterInt.invoke(newpc, new Integer(8)); // assume int from PIT for now
                        fast = (int[])state1.invoke(newpc);
                        System.out.println("Forced enter interrupt.");
                        if (bochsState[8] == 0xfea6)
                        {
                            System.out.println("HACK to follow bizarre Bochs behaviour");
                            // HACK to follow boch's bizarre (wrong?) behaviour here
                            execute1.invoke(newpc); // will do STI and following instruction, then take away one tick
                            ticksDelta.invoke(newpc, new Integer(-1));
                            fast = (int[])state1.invoke(newpc);
                            nextBochs = bochs.executeInstruction(); // will do instruction following STI
                            bochsState = bochs.getState();
                        }
                    }
                }
            }

            Set<Integer> diff = new HashSet<Integer>();
            if (!sameStates(fast, bochsState, compareFlags, diff))
            {
                if ((diff.size() == 1) && diff.contains(9))
                {
                    // adopt flags
                    String prevInstr = ((String)(history[(historyIndex-2)&(history.length-1)][2])).split(" ")[0];
                    String secondPrevInstr = ((String)(history[(historyIndex-3)&(history.length-1)][2])).split(" ")[0];
                    if (prevInstr.startsWith("rep"))
                        prevInstr += ((String)(history[(historyIndex-2)&(history.length-1)][2])).split(" ")[1];
                    if (prevInstr.startsWith("cli") || secondPrevInstr.startsWith("cli"))
                    {
                         if ((fast[9]^bochsState[9]) == 0x200)
                         {
                             fast[9] = bochsState[9];
                             setState1.invoke(newpc, (int[])fast);
                         }
                    }

                    if (previousLss)
                    {
                        previousLss = false;
                        fast[9] = bochsState[9];
                        setState1.invoke(newpc, (int[])fast);
                    }
                    else if (flagIgnores.containsKey(prevInstr))
                    {
                        int mask = flagIgnores.get(prevInstr);
                        if ((fast[9]& mask) == (bochsState[9] & mask))
                        {
                            fast[9] = bochsState[9];
                            setState1.invoke(newpc, (int[])fast);
                        }
                    } else if ((fast[9]& flagAdoptMask) == (bochsState[9] & flagAdoptMask))
                    {
                        fast[9] = bochsState[9];
                        setState1.invoke(newpc, (int[])fast);
                    }
                    if (prevInstr.equals("lss"))
                        previousLss = true;

                }
                else if ((diff.size() == 1) && diff.contains(0))
                {
                    if ((fast[0]^bochsState[0]) == 0x10)
                    {
                        //often eax is loaded with flags which contain arbitrary AF values, ignore these
                        fast[0] = bochsState[0];
                        setState1.invoke(newpc, (int[])fast);
                    }
                    else if (previousInstruction().startsWith("in ")) // IO port read
                    {
                        // print before and after state, then adopt reg
                        if (!ignoredIOPorts.contains(fast[2])) // port is in dx
                        {
                            System.out.printf("IO read difference: port=%08x eax=%08x#%08x from %s\n", fast[2], fast[0], bochsState[0], previousInstruction());
                            //printLast2();
                        }
                        fast[0] = bochsState[0];
                        setState1.invoke(newpc, (int[])fast);
                    }
                }
                else if ((fast[0] >= 0xa8000) && (fast[0] < 0xb0000) && previousInstruction(1).startsWith("movzx edx,BYTE PTR [eax]")) // see smm_init in rombios32.c
                {
                    fast[2] = bochsState[2];
                    setState1.invoke(newpc, (int[])fast);
                }
                else if ((previousState()[2] == 0xb2) && previousInstruction(2).startsWith("out dx")) // entered SMM
                {
                    String bochsDisam = nextBochs;
                    String prev = null;
                    while (fast[8] != bochsState[8])
                    {
                        prev = bochsDisam;
                        bochsDisam = bochs.executeInstruction();
                        bochsState = bochs.getState();
                    }
                    fast[16] = bochsState[16];
                    setState1.invoke(newpc, (int[])fast);
                    System.out.println("Remote returned from SMM with: "+prev + " and ticks: "+fast[16]);
                }
                diff.clear();
                if (!sameStates(fast, bochsState, compareFlags, diff))
                {
                    printHistory();
                    for (int diffIndex: diff)
                        System.out.printf("Difference: %s %08x - %08x : ^ %08x\n", names[diffIndex], fast[diffIndex], bochsState[diffIndex], fast[diffIndex]^bochsState[diffIndex]);
                        setState1.invoke(newpc, (int[])bochsState);
                    if (diff.contains(8))
                    {
                        printPITs((int[]) pit1.invoke(newpc), bochs.getPit());
                        throw new IllegalStateException("Different EIP!");
                    }
                }
            }
            // compare other devices
            if (compareCMOS)
            {
                // CMOS
                byte[] jpcCMOS = (byte[]) cmos1.invoke(newpc);
                byte[] bochsCMOS = bochs.getCMOS();
                boolean same = true;
                for (int i=0; i < 128; i++)
                {
                    if (jpcCMOS[i] != bochsCMOS[i])
                    {
                        same = false;
                        break;
                    }
                }
                if (!same)
                {
                    printLast2();
                    System.out.println("Different CMOS");
                    System.out.println("JPC CMOS :: Bochs CMOS");
                    for (int i=0; i < 8; i++)
                    {
                        System.out.printf("%02x = ", i*16);
                        for (int j=0; j < 16; j++)
                            System.out.printf("%02x ", jpcCMOS[i*16+j]);
                        System.out.printf("= ");
                        for (int j=0; j < 16; j++)
                            System.out.printf("%02x ", bochsCMOS[i*16+j]);
                        System.out.println();
                    }
                    throw new IllegalStateException("Different CMOS");
                }
            }
            if (comparePIT)
            {
                lastPIT0Count = comparePITS(lastPIT0Count, bochs, newpc, pit1);
            }
            if (compareStack)
            {
                boolean pm = (fast[36] & 1) != 0;
                int ssBase = fast[35];
                int esp = fast[6] + ssBase;
                int espPageIndex;
                if (pm)
                    espPageIndex = esp;
                else
                    espPageIndex = esp >>> 12;
                if (previousStackAddr != espPageIndex)
                {
                    // we've changed stacks, compare the old one as well
                    compareStacks(previousStackAddr, previousStackAddr, save1, newpc, sdata1, bochs, sdata2, pm, load1);

                    previousStackAddr = espPageIndex;
                }

                compareStacks(espPageIndex, esp, save1, newpc, sdata1, bochs, sdata2, pm, load1);
            }
            if (!mem)
                continue;
            Set<Integer> dirtyPages = new HashSet<Integer>();
            dirty1.invoke(newpc, dirtyPages);
            //for (int i=0; i < 2*1024; i++)
            //    dirtyPages.add(i);
            if (dirtyPages.size() > 0)
            {
                System.out.printf("Comparing");
                for (int i: dirtyPages)
                    System.out.printf(" %08x", i << 12);
                System.out.println(" after " + previousInstruction());
            }
            for (int i : dirtyPages)
            {
                Integer l1 = (Integer)save1.invoke(newpc, new Integer(i<<12), sdata1, false);
                Integer l2 = bochs.savePage(new Integer(i<<12), sdata2, false);
                if (l2 > 0)
                    if (!samePage(i, sdata1, sdata2, null))
                    {
                        printHistory();
                        System.out.println("Error here... look above");
                        printPage(sdata1, sdata2, i << 12);
                        if (continueExecution("memory"))
                            load1.invoke(newpc, new Integer(i), sdata2, false);
                        else
                            System.exit(0);
                    }
            }
        }
    }

    private static boolean inInt(int[] prev, int[] curr)
    {
        int prevESP = prev[4];
        int ESP = curr[4];
        int prevEIP = prev[8];
        int EIP = curr[8];
//        if (Math.abs(ESP-prevESP) < 4) STI, POP would give =4
//            return false;
        if ((EIP == 0xfea5) || (EIP == 0xfea6))
            return true;
        return false;
    }

    private static int comparePITS(int lastPIT0Count, EmulatorControl bochs, Object newpc, Method pit1) throws Exception
    {
        int[] jpcPIT = (int[]) pit1.invoke(newpc);
        int[] bochsPIT = bochs.getPit();
        if (bochsPIT[0] != lastPIT0Count)
        {
            lastPIT0Count = bochsPIT[0];
            boolean same = true;
            for (int i=0; i < jpcPIT.length; i++)
            {
                if ((jpcPIT[i] != bochsPIT[i]) && (i % 4 != 3)) // ignore next_change_time slot
                {
                    same = false;
                    break;
                }
            }
            if (!same)
            {
                printLast2();
                System.out.println("Different PIT");
                printPITs(jpcPIT, bochsPIT);
            }
        }
        return lastPIT0Count;
    }

    private static void printPITs(int[] jpcPIT, int[] bochsPIT)
    {
        System.out.println("JPC Pit :: Bochs Pit");
        for (int i=0; i < 3; i++)
        {
            for (int j=0; j < 4; j++)
            {
                System.out.printf("%08x ", jpcPIT[i*4+j]);
                System.out.printf("= ");
                System.out.printf("%08x ", bochsPIT[i*4+j]);
                System.out.println();
            }
        }
    }

    private static void compareStacks(int espPageIndex, int esp, Method save1, Object newpc, byte[] sdata1, EmulatorControl bochs,byte[] sdata2, boolean pm, Method load1) throws Exception
    {
        Integer sl1 = (Integer)save1.invoke(newpc, new Integer(espPageIndex), sdata1, pm);
        Integer sl2 = bochs.savePage(new Integer(espPageIndex), sdata2, pm);
        List<Integer> addrs = new ArrayList();
        if (sl2 > 0)
            if (!samePage(espPageIndex, sdata1, sdata2, addrs))
            {
                int addr = addrs.get(0);
                if ((addrs.size() == 1) && ((sdata1[addr]^sdata2[addr]) == 0x10))
                { // ignore differences from pushing different AF to stack
                    System.out.println("ignoring different AF on stack...");
                    load1.invoke(newpc, new Integer(espPageIndex), sdata2, pm);
                }
                else
                {
                    printHistory();
                    System.out.println("Error here... look above");
                    printPage(sdata1, sdata2, esp);
                    load1.invoke(newpc, new Integer(espPageIndex), sdata2, pm);
                }
            }
    }

    private static String currentInstruction()
    {
        Object[] prev = history[(((historyIndex-1)%history.length) + history.length) % history.length];
        if (prev == null)
            return "null";
        return (String)prev[2];
    }

    private static String previousInstruction()
    {
        return previousInstruction(1);
    }

    private static String previousInstruction(int i)
    {
        Object[] prev = history[(((historyIndex-(1+i))%history.length) + history.length) % history.length];
        if (prev == null)
            return "null";
        return (String)prev[2];
    }

    private static int[] previousState()
    {
        Object[] prev = history[(((historyIndex-2)%history.length) + history.length) % history.length];
        if (prev == null)
            return null;
        return (int[])prev[0];
    }

    static Object[][] history = new Object[10][];
    static int historyIndex=0;

    private static void printLast2()
    {
        int index2 = decrementHistoryIndex(historyIndex);
        int index1 = decrementHistoryIndex(index2);
        printState(history[index1]);
        printState(history[index2]);
    }

    private static int decrementHistoryIndex(int index)
    {
        return (index-1+history.length)%history.length;
    }

    private static void printHistory()
    {
        printState(history[historyIndex]);
        int end = historyIndex;
        for (int j = (end+1)%history.length; j != end ; j = (j+1)%history.length)
        {
            printState(history[j]);
        }
    }

    private static void printState(Object s)
    {
        if (s == null)
            return;
        Object[] sarr = (Object[]) s;
        int[] fast = (int[]) sarr[0];
        int[] old = (int[]) sarr[1];
        String line = (String) sarr[2];
        System.out.println("New JPC:");
        Fuzzer.printState(fast);
        System.out.println("Old JPC:");
        Fuzzer.printState(old);
        System.out.println(line);
    }

    public static void printPage(byte[] fast, byte[] old, int esp)
    {
        int address = esp&0xfffff000;
        // print page
        for (int i=0; i < 1 << 8; i++)
        {
            int v1 = getInt(fast, 16*i);
            int v2 = getInt(fast, 16*i+4);
            int v3 = getInt(fast, 16*i+8);
            int v4 = getInt(fast, 16*i+12);
            int r1 = getInt(old, 16*i);
            int r2 = getInt(old, 16*i+4);
            int r3 = getInt(old, 16*i+8);
            int r4 = getInt(old, 16*i+12);

            System.out.printf("0x%8x:  %8x %8x %8x %8x -- %8x %8x %8x %8x ==== ", address + 16*i, v1, v2, v3, v4, r1, r2, r3, r4);
            printIntChars(v1, r1);
            printIntChars(v2, r2);
            printIntChars(v3, r3);
            printIntChars(v4, r4);
            System.out.print(" -- ");
            printIntChars(r1, v1);
            printIntChars(r2, v2);
            printIntChars(r3, v3);
            printIntChars(r4, v4);
            System.out.println();
        }

        // print differences
        for (int i =0; i < 1<< 12; i++)
        {
            byte b1 = fast[i];
            byte b2 = old[i];
            if (b1 != b2)
            {
                System.out.println("Memory difference at 0x" + Integer.toHexString(address+i) + ", values: " + Integer.toHexString(b1 & 0xff) + " " + Integer.toHexString(b2 & 0xff));
            }
        }
    }

    public static int getInt(byte[] data, int offset)
    {
        return data[offset] & 0xff | ((data[offset+1] & 0xff) << 8)  | ((data[offset+2] & 0xff) << 16)  | ((data[offset+3] & 0xff) << 24);
    }

    public static void printIntChars(int i, int c)
    {
        int[] ia = new int[] {(i & 0xFF), ((i >> 8) & 0xFF), ((i >> 16) & 0xFF), ((i >> 24) & 0xFF)};
        int[] ca = new int[] {(c & 0xFF), ((c >> 8) & 0xFF), ((c >> 16) & 0xFF), ((c >> 24) & 0xFF)};

        for (int a = 0; a < 4; a++)
            if (ia[a] == ca[a])
                System.out.printf("%c", (ia[a] == 0 ? ' ' : (char)ia[a]));
            else
                System.out.printf("\u001b[1;44m%c\u001b[1;49m", (ia[a] == 0 ? ' ' : (char)ia[a]));
        System.out.printf(" ");
    }

    public static boolean samePage(int index, byte[] fast, byte[] old, List<Integer> addrs)
    {
        if (fast.length != old.length)
            throw new IllegalStateException(String.format("different page data lengths %d != %d", fast.length, old.length));
        for (int i=0; i < fast.length; i++)
            if (fast[i] != old[i])
            {
                if (addrs!= null)
                    addrs.add(i);
                System.out.printf("Difference in memory state: %08x=> %02x - %02x\n", index*4096+i, fast[i], old[i]);
                return false;
            }
        return true;
    }

    public static boolean sameStates(int[] fast, int[] old, boolean compareFlags, Set<Integer> diff)
    {
        if (fast.length != names.length)
            throw new IllegalArgumentException(String.format("new state length: %d != %d",fast.length, names.length));
        if (old.length != names.length)
            throw new IllegalArgumentException("old state length = "+old.length);
        boolean same = true;
        for (int i=0; i < fast.length; i++)
            if (i != 9)
            {
                if (fast[i] != old[i])
                {
                    diff.add(i);
                    same = false;
                }
            }
            else
            {
                if (compareFlags && ((fast[i]&flagMask) != (old[i]&flagMask)))
                {
                    if (same)
                    {
                        same = false;
                        diff.add(i);
                    }
                }
            }
        return same;
    }

    public static boolean continueExecution(String state)
    {
        System.out.println("Adopt "+state+"? (y/n)");
        String line = null;
        try {
            line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException f)
        {
            f.printStackTrace();
            System.exit(0);
        }
        if (line.equals("y"))
            return true;
        else
            return false;
    }

    public static class MouseEvent implements Comparable<MouseEvent>
    {
        public final long time;
        public final int dx, dy, dz;
        public final int buttons;

        MouseEvent(long time, int dx, int dy, int dz, boolean leftDown, boolean middleDown, boolean rightDown)
        {
            this.time = time;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            int buttons = 0;
            if (leftDown)
                buttons |= 1;
            if (middleDown)
                buttons |= 2;
            if (rightDown)
                buttons |= 4;
            this.buttons = buttons;
        }

        public int compareTo(MouseEvent o)
        {
            return (int)(time - o.time);
        }
    }

    public static class KeyBoardEvent implements Comparable<KeyBoardEvent>
    {
        public final long time;
        public final String text;

        KeyBoardEvent(long time, String text)
        {
            this.time = time;
            this.text = text;
        }

        public int compareTo(KeyBoardEvent o)
        {
            return (int)(time - o.time);
        }
    }
}