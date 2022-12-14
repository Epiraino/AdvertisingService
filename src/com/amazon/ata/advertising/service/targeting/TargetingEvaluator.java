package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }


    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
        boolean allTruePredicates = true;
        /***************************************************************************************************************
         * INSERT THREAD POOL HERE
         *
         *
         *
         **************************************************************************************************************/
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Boolean>> networkCallResults = new ArrayList<Future<Boolean>>();


        networkCallResults.add(
        executor.submit(() -> targetingPredicates.stream()
                        .allMatch(targetingPredicate -> targetingPredicate.evaluate(requestContext).isTrue()))
        );

        executor.shutdown();
        for (Future<Boolean> result : networkCallResults){
            try {
                if (!result.get()) {
                    allTruePredicates = false;
                }
            } catch(InterruptedException | ExecutionException e){
                e.printStackTrace();
            }


        }
//                   allTruePredicates = targetingPredicates.stream()
//                    .allMatch(targetingPredicate -> targetingPredicate.evaluate(requestContext).isTrue());





// OUTDATED
//        for (TargetingPredicate predicate : targetingPredicates) {
//            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
//            if (!predicateResult.isTrue()) {
//                allTruePredicates = false;
//                break;
//            }
//        }
        /***************************************************************************************************************
         * This is the Section we need to change
         **************************************************************************************************************/

        return allTruePredicates ? TargetingPredicateResult.TRUE :
                                   TargetingPredicateResult.FALSE;
    }
}
