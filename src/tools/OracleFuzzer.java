/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine

    Copyright (C) 2012-2013 Ian Preston

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at:

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    End of licence header
*/
package tools;

import org.jpc.emulator.execution.decoder.Disassembler;
import org.jpc.emulator.execution.decoder.Instruction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class OracleFuzzer
{
    public static final int RM = 1;
    public static final int PM = 2;
    public static final int VM = 3;

    public static void main(String[] args) throws IOException
    {
        String[] pcargs = new String[] {"-max-block-size", "1", "-boot", "hda", "-hda", "linux.img"};
        EmulatorControl disciple = new JPCControl(CompareToBochs.newJar, pcargs);
        EmulatorControl oracle = new Bochs("linux.cfg");

        // set cs base to 0
        oracle.executeInstruction(); // jmp 0000:2000

        //set up the exception handlers so we can tell from EIP which exception occurred


        // test Real mode


        // test Protected mode


        // test Real mode with 32 bit segments after return from Protected mode


        // test Virtual 8086 mode


        int codeEIP = 0x2000;
        int[] inputState = new int[CompareToBochs.names.length];
        inputState[0] = 0x12345678;
        inputState[1] = 0x9ABCDEF0;
        inputState[2] = 0x192A3B4C;
        inputState[3] = 0x5D6E7F80;
        inputState[4] = 0x800; // esp
        inputState[5] = 0x15263748;
        inputState[6] = 0x9DAEBFC0;
        inputState[7] = 0x15263748;
        inputState[8] = codeEIP; // eip
        inputState[9] = 0x846; // eflags
        inputState[10] = 0x3000; // es
        inputState[11] = 0x0; // cs
        inputState[12] = 0x4000; // ds
        inputState[13] = 0x5000; // ss
        inputState[14] = 0x6000; // fs
        inputState[15] = 0x7000; // gs
        // RM segment limits (not used)
        for (int i=0; i < 6; i++)
            inputState[17 + i] = 0xffff;
        inputState[25] = inputState[27] = inputState[29] = 0xffff;
        inputState[36] = 0x60000010; // CR0

        // FPU
        long one = getDoubleBits(1.0);
        long two = getDoubleBits(2.0);
        long four = getDoubleBits(4.0);
        long eight = getDoubleBits(8.0);
        long sixteen = getDoubleBits(16.0);
        long half = getDoubleBits(0.5);
        long hundred = getDoubleBits(100.0);
        long thousand = getDoubleBits(1000.0);
        inputState[37] = (int) (one >> 32); // ST0H
        inputState[38] = (int) one; // ST0L
        inputState[39] = (int) (two >> 32); // ST1H
        inputState[40] = (int) two; // ST1L
        inputState[41] = (int) (four >> 32); // ST2H
        inputState[42] = (int) four; // ST2L
        inputState[43] = (int) (eight >> 32); // ST3H
        inputState[44] = (int) eight; // ST3L
        inputState[45] = (int) (sixteen >> 32); // ST4H
        inputState[46] = (int) sixteen; // ST4L
        inputState[47] = (int) (half >> 32); // ST5H
        inputState[48] = (int) half; // ST5L
        inputState[49] = (int) (hundred >> 32); // ST6H
        inputState[50] = (int) hundred; // ST6L
        inputState[51] = (int) (thousand >> 32); // ST7H
        inputState[52] = (int) thousand; // ST7L

        byte[] code = new byte[16];
        for (int i=0; i < 16; i++)
            code[i] = (byte)i;

        int cseip = codeEIP;

        for (int i=0; i < 256; i++)
            for (int j=0; j < 256; j++)
            {
                if (i == 0xF4) // don't test halt
                        continue;
                // don't 'test' segment overrides
                if ((i == 0x26) || (i == 0x2e) || (i == 0x36) || (i == 0x3e) || (i == 0x64) || (i == 0x65))
                    continue;
                if (i == 0xf0) // don't test lock
                    continue;
                if ((i == 0xf2) || (i == 0xf3)) // don't test rep/repne
                    continue;
                if ((i == 0x66) || (i == 0x67)) // don't test size overrides
                    continue;
                if ((i == 0xe4) || (i == 0xe5) || (i == 0xec) || (i == 0xed)) // don't test in X,Ib
                        continue;
                if ((i == 0xe6) || (i == 0xe7) || (i == 0xee) || (i == 0xef)) // don't test out Ib,X
                        continue;
                if (i == 0x17) // don't test pop ss
                    continue;

                if ((j == 0xf4) && (i == 0x17)) // avoid a potential halt after a pop ss which forces a 2nd instruction
                    continue;
                code[0] = (byte) i;
                code[1] = (byte) j;
                cseip = testOpcode(disciple, oracle, cseip, code, 1, inputState, 0xffffffff, RM);
            }

        byte[] prefices = new byte[]{(byte) 0x66, 0x67, 0x0F};
        for (byte p: prefices)
        {
            code[0] = p;
            for (int i=0; i < 256; i++)
                for (int j=0; j < 256; j++)
                {
                    if (i == 0xF4) // don't test halt
                        continue;
                    // don't 'test' segment overrides
                    if ((i == 0x26) || (i == 0x2e) || (i == 0x36) || (i == 0x3e) || (i == 0x64) || (i == 0x65))
                        continue;
                    if (i == 0xf0) // don't test lock
                        continue;
                    if ((i == 0xf2) || (i == 0xf3)) // don't rep/repne
                        continue;
                    if ((i == 0x66) || (i == 0x67)) // don't test size overrides
                        continue;
                    if ((i == 0xe4) || (i == 0xe5) || (i == 0xec) || (i == 0xed)) // don't test in X,Ib
                        continue;
                    if ((i == 0xe6) || (i == 0xe7) || (i == 0xee) || (i == 0xef)) // don't test out Ib,X
                        continue;
                    if (i == 0x17) // don't test pop ss
                        continue;

                    if ((j == 0xf4) && (i == 0x17)) // avoid a potential halt after a pop ss which forces a 2nd instruction
                        continue;
                    code[1] = (byte) i;
                    code[2] = (byte) j;
                    cseip = testOpcode(disciple, oracle, cseip, code, 1, inputState, 0xffffffff, RM);
                }
        }
    }

    // returns resulting cseip
    private static int testOpcode(EmulatorControl disciple, EmulatorControl oracle, int currentCSEIP, byte[] code, int x86, int[] inputState, int flagMask, int mode) throws IOException
    {
        disciple.setState(inputState, 0);
        if (code[0] == (byte) 0x9b)
            System.out.println("Here!");
        oracle.setState(inputState, currentCSEIP);

        disciple.setPhysicalMemory(inputState[8], code);
        oracle.setPhysicalMemory(inputState[8], code);

        try {
            for (int i=0; i < x86; i++)
            {
                disciple.executeInstruction();
                oracle.executeInstruction();
            }
        } catch (RuntimeException e)
        {
            System.out.println("*****************ERROR****************");
            System.out.println(e.getMessage());
            return inputState[31];
        }
        int[] us = disciple.getState();
        int[] good = oracle.getState();

        if (!sameState(us, good, flagMask))
            printCase(code, x86, mode, disciple, inputState, us, good, flagMask);
        return good[31]+good[8];
    }

    public static long getDoubleBits(double x)
    {
        return Double.doubleToLongBits(x);
    }

    private static boolean sameState(int[] disciple, int[] oracle, int flagMask)
    {
        for (int i=0; i < disciple.length; i++)
        {
            if (i == 9)
            {
                if ((disciple[i] & flagMask) != (oracle[i] & flagMask))
                    return false;
            }
            else if (i == 16) // ignore ticks
                continue;
            else if (disciple[i] != oracle[i])
                return false;
        }
        return true;
    }

    private static Set<Integer> differentRegs(int[] disciple, int[] oracle, int flagMask)
    {
        Set<Integer> diff = new TreeSet();
        for (int i=0; i < disciple.length; i++)
        {
            if (i == 9)
            {
                if ((disciple[i] & flagMask) != (oracle[i] & flagMask))
                    diff.add(i);
            }
            else if (i == 16) // ignore ticks
                continue;
            else if (disciple[i] != oracle[i])
                diff.add(i);
        }
        return diff;
    }

    private static void printCase(byte[] code, int x86, int mode, EmulatorControl disciple, int[] input, int[] discipleState, int[] oracle, int flagMask)
    {
        System.out.println("***************Test case error************************");
        System.out.println("Code:");
        for (byte b: code)
            System.out.printf("%02x ", b);
        System.out.println();
        System.out.println(disciple.disam(code, x86, mode));
        System.out.println("Differences:");
        Set<Integer> diff = differentRegs(discipleState, oracle, flagMask);
        for (Integer index: diff)
            System.out.printf("Difference: %s %08x - %08x : ^ %08x\n", CompareToBochs.names[index], discipleState[index], oracle[index], discipleState[index]^oracle[index]);
        System.out.println("Input:");
        Fuzzer.printState(input);
        System.out.println("Disciple:");
        Fuzzer.printState(discipleState);
        System.out.println("Oracle:");
        Fuzzer.printState(oracle);
    }
}