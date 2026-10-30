package org.practice.answers.alice.q10;

import org.practice.api.Q10AbaDemo;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Q10 答案：Treiber 栈上的 ABA 复现与 AtomicStampedReference 修复。
 */
public final class Q10AnswerImpl implements Q10AbaDemo {

    private static final class Node {
        private final String value;
        private Node next;

        private Node(String value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    @Override
    public boolean reproduceWithAtomicReference() {
        AtomicReference<Node> head = new AtomicReference<>();
        head.set(new Node("A", null));
        head.set(new Node("B", head.get()));
        head.set(new Node("C", head.get()));

        Node top = head.get();   // 线程 1 读到的栈顶 C
        Node next = top.next;    // 线程 1 读到的第二个节点 B

        // 线程 2：弹出 C、弹出 B，再把 C 压回去（此时栈为 C -> A）
        head.set(next);          // 弹出 C，栈顶变为 B
        head.set(next.next);     // 弹出 B，栈顶变为 A
        top.next = head.get();   // C.next 指向 A
        head.set(top);           // 压回 C，栈顶引用与线程 1 读到的完全相同

        // 线程 1 的 CAS 因引用相同而错误地成功：此时 B 早已被弹出，栈已损坏
        return head.compareAndSet(top, next);
    }

    @Override
    public boolean fixWithAtomicStampedReference() {
        AtomicStampedReference<Node> head = new AtomicStampedReference<>(new Node("A", null), 0);
        push(head, "B");
        push(head, "C");

        int[] stamp = new int[1];
        Node top = head.get(stamp);   // 栈顶 C，版本号 stamp[0]
        Node next = top.next;         // 第二个节点 B

        // 线程 2：同样的弹出/压回序列，每次修改都让版本号 +1
        Node poppedC = pop(head);
        pop(head);
        int[] current = new int[1];
        Node nowTop = head.get(current);
        poppedC.next = nowTop;
        head.compareAndSet(nowTop, poppedC, current[0], current[0] + 1);

        // 线程 1 携带旧版本号做 CAS：引用相同但版本号已变，CAS 失败，ABA 被拦截
        return head.compareAndSet(top, next, stamp[0], stamp[0] + 1);
    }

    private static void push(AtomicStampedReference<Node> head, String value) {
        int[] stamp = new int[1];
        while (true) {
            Node current = head.get(stamp);
            int expectedStamp = stamp[0];
            if (head.compareAndSet(current, new Node(value, current), expectedStamp, expectedStamp + 1)) {
                return;
            }
        }
    }

    private static Node pop(AtomicStampedReference<Node> head) {
        int[] stamp = new int[1];
        while (true) {
            Node current = head.get(stamp);
            if (current == null) {
                return null;
            }
            int expectedStamp = stamp[0];
            if (head.compareAndSet(current, current.next, expectedStamp, expectedStamp + 1)) {
                return current;
            }
        }
    }
}
