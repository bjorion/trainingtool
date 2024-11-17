package org.jorion.trainingtool.training;

import org.jorion.trainingtool.type.Provider;

public class RandomTraining {

    public static Training.TrainingBuilder buildTraining() {

        return Training.builder()
                .id(1L)
                .title("UnitTest: Learn Java in 21 Days")
                .description("UnitTest: Java for Beginners")
                .provider(Provider.CEVORA)
                .url("http://www.cevora.be")
                .enabled(true);

    }
}
