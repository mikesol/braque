package braque.internal.codegen;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A random string generator.
 * Created by mikesolomon on 21/09/16.
 */
final class RandGen {
    private String currentRandom = next();

    synchronized String current() {
        return currentRandom;
    }

    synchronized String next() {
        currentRandom = RandomStringUtils.randomAlphabetic(32).toLowerCase();
        return currentRandom;
    }
}
