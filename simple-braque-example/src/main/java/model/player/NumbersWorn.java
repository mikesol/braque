package model.player;

import braque.Clojure;
import braque.PropertyList;

/**
 * Created by mikesolomon on 20/09/16.
 */
@Clojure("(if (= path \"/players/*/numbersworn\") '(\"@Deprecated\") '())")
@PropertyList(Integer.class)
public interface NumbersWorn {
}
