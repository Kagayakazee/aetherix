package xd.kagayakazee.aetherix.utils;


import org.bukkit.potion.PotionEffectType;
//chatgpt
public class VersionAdapter {


    public static final PotionEffectType JUMP_BOOST;
    public static final PotionEffectType SLOWNESS;


    static {

        JUMP_BOOST = getPotionEffectType("JUMP_BOOST", "JUMP");
        SLOWNESS = getPotionEffectType("SLOW");

    }

    private static PotionEffectType getPotionEffectType(String modernName, String... legacyNames) {
        try {

            return PotionEffectType.getByName(modernName);
        } catch (IllegalArgumentException e) {

            for (String legacyName : legacyNames) {
                try {
                    return PotionEffectType.getByName(legacyName);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }


        return null;
    }
}