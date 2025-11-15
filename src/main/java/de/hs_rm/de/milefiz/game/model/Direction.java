package de.hs_rm.de.milefiz.game.model;

public enum Direction {
    NORTH {
        @Override
        public Direction getOpposite(){
            return SOUTH;
        }
    }, EAST {
        @Override
        public Direction getOpposite() {
            return WEST;
        }
    }, SOUTH {
        @Override
        public Direction getOpposite() {
            return NORTH;
        }
    }, WEST {
        @Override
        public Direction getOpposite() {
            return EAST;
        }
    };

    /**
     * 
     * @return die entgegengesetzte Richtung
     */
    public abstract Direction getOpposite();
}
