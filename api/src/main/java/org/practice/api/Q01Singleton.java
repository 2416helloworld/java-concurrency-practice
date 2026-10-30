package org.practice.api;

/**
 * Q01：线程安全的单例。
 *
 * <p>实现类需满足：
 * <ul>
 *   <li>实现本接口；</li>
 *   <li>构造函数不允许是 {@code public}（禁止外部直接 new）；</li>
 *   <li>提供 {@code public static Q01Singleton getInstance()}，
 *       多次调用（包括并发调用）必须返回同一个实例。</li>
 * </ul>
 */
public interface Q01Singleton {
}
