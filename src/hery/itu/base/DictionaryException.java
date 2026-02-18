package hery.itu.base;

import hery.itu.huffman.DictionaryValidator;

/** Entrée refusée par les règles du dictionnaire (voir {@link DictionaryValidator}). */
public class DictionaryException extends Exception {

    private final DictionaryValidator.Problem problem;

    public DictionaryException(DictionaryValidator.Problem problem) {
        super("Entrée refusée : " + problem);
        this.problem = problem;
    }

    public DictionaryValidator.Problem getProblem() {
        return problem;
    }
}
