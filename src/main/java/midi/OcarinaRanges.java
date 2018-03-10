package midi;

public class OcarinaRanges {

    public static int getLowerRange(Ocarinas ocarina) {
        switch(ocarina) {
            case C_SOPRANO:
                return 69;

            default:
                return 89;
        }
    }

    public static int getUpperRange(Ocarinas ocarina) {
        switch(ocarina) {
            case C_SOPRANO:
                return 101;

            default:
                return 0;
        }
    }
}
