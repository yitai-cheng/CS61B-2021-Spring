package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";

    public static void main(String[] args) {
        GuitarString[] guitarStrings = new GuitarString[keyboard.length()];
        for (int i = 0; i < guitarStrings.length; i++) {
            guitarStrings[i] = new GuitarString(440 * Math.pow(2, (i - 24.0) / 12.0));
        }
        while (true) {
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (isValidKey(key)) {
                    guitarStrings[keyboard.indexOf(key)].pluck();
                }
            }

            /* compute the superposition of samples */
            double sample = 0;
            for (GuitarString guitarString : guitarStrings) {
                sample += guitarString.sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (GuitarString guitarString : guitarStrings) {
                guitarString.tic();
            }
        }
    }

    public static boolean isValidKey(char key) {
        for (char c : keyboard.toCharArray()) {
            if (c == key) {
                return true;
            }
        }
        return false;
    }
}

