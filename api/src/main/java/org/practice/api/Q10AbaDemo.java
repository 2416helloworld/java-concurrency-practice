package org.practice.api;

/**
 * Q10：ABA 问题复现与解决。
 *
 * <p>两个方法都要用 Treiber 无锁栈执行同一段场景：
 * 线程 1 读到栈顶 A，线程 2 把 A 弹出、又压回 A（中间经过其他节点），线程 1 再对栈顶做 CAS。
 *
 * @return 是否“错误地”CAS 成功——true 表示发生了 ABA，false 表示被版本号成功拦截
 */
public interface Q10AbaDemo {

    /**
     * 用 {@code AtomicReference} 实现栈顶 CAS，复现 ABA。
     */
    boolean reproduceWithAtomicReference();

    /**
     * 用 {@code AtomicStampedReference} 实现栈顶 CAS，用版本号阻止 ABA。
     */
    boolean fixWithAtomicStampedReference();
}
