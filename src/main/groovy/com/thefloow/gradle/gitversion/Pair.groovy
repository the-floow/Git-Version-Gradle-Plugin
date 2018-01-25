package com.thefloow.gradle.gitversion

import groovy.transform.Canonical

import static com.google.common.base.Preconditions.checkArgument

@Canonical
class Pair<A, B> {

    A first;
    B second;

    Pair(A first, B second) {
        checkArgument(first != null, "The first parameter must not be null.");
        checkArgument(second != null, "The second parameter must not be null.");

        this.first = first;
        this.second = second;
    }


    static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

}
