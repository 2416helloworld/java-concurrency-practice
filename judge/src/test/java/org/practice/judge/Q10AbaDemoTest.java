package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q10AbaDemo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q10AbaDemoTest {

    @Test
    void atomicReferenceAllowsAba() {
        Q10AbaDemo demo = Answers.newAnswer("q10", "Q10AnswerImpl", Q10AbaDemo.class);
        assertTrue(demo.reproduceWithAtomicReference(), "AtomicReference 版本的 CAS 应被 ABA 欺骗");
    }

    @Test
    void stampedReferenceBlocksAba() {
        Q10AbaDemo demo = Answers.newAnswer("q10", "Q10AnswerImpl", Q10AbaDemo.class);
        assertFalse(demo.fixWithAtomicStampedReference(), "版本号变化后 CAS 必须失败");
    }
}
