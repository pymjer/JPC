package org.jpc.emulator.execution.opcodes.pm;

import org.jpc.emulator.execution.*;
import org.jpc.emulator.execution.decoder.*;
import org.jpc.emulator.processor.*;
import org.jpc.emulator.processor.fpu64.*;
import static org.jpc.emulator.processor.Processor.*;

public class call_o16_Ep_mem extends Executable
{
        final Pointer seg, offset;    final int blockLength;
    final int instructionLength;

    public call_o16_Ep_mem(int blockStart, Instruction parent)
    {
        super(blockStart, parent);
        blockLength = parent.x86Length+(int)parent.eip-blockStart;
        instructionLength = parent.x86Length;
        offset = new Pointer(parent.operand[0], parent.adr_mode);
        parent.operand[0].lval += 2;
        seg = new Pointer(parent.operand[0], parent.adr_mode);
    }

    public Branch execute(Processor cpu)
    {
        int cs = seg.get16(cpu);
        int targetEip = offset.get16(cpu);
                cpu.eip += blockLength;
        if (cpu.ss.getDefaultSizeFlag())
            throw new IllegalStateException("pm call far o16 a32 not implemented");
        else
            cpu.call_far_pm_o16_a16(0xffff & cs, 0xffff & targetEip);
        return Branch.Call_Unknown;
    }

    public boolean isBranch()
    {
        return true;
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}