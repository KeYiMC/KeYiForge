package gg.m2ke4u.keyicore.utils;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

public class ConcurrentlyTraverse<E> extends RecursiveAction {
    private final Spliterator<E> spliterator;
    private final Consumer<E> action;
    private final long threshold;

    public ConcurrentlyTraverse(Iterable<E> iterable,int threads,Consumer<E> action){
        this.spliterator = iterable.spliterator();
        this.action = action;
        this.threshold = (int)iterable.spliterator().getExactSizeIfKnown() / threads;
    }

    private ConcurrentlyTraverse(Spliterator<E> spliterator,Consumer<E> action,long t) {
        this.spliterator = spliterator;
        this.action = action;
        this.threshold = t;
    }

    @Override
    protected void compute() {
        if (this.spliterator.getExactSizeIfKnown() <= this.threshold) {
            this.spliterator.forEachRemaining(o->{
                try {
                    this.action.accept(o);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        } else {
            new ConcurrentlyTraverse<>(this.spliterator.trySplit(), this.action, this.threshold).fork();
            new ConcurrentlyTraverse<>(this.spliterator, this.action, this.threshold).fork();
        }
    }

    public static <E> ConcurrentlyTraverse<E> wrapNewAndRun(Collection<E> collection, Consumer<E> action, ForkJoinPool pool){
        return (ConcurrentlyTraverse<E>) pool.submit(new ConcurrentlyTraverse<>(collection,pool.getParallelism(),action));
    }
}
