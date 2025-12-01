package de.hs_rm.de.milefiz.game.model;

public enum FieldType {
    NORMAL{
        @Override
        public boolean isStart() {
            return false;
        }
    }, 
    START_RED{
        @Override
        public boolean isStart() {
            return true;
        }        
    },
    START_YELLOW{
        @Override
        public boolean isStart() {
            return true;
        }        
    },
    START_BLUE{
        @Override
        public boolean isStart() {
            return true;
        }
    },
    START_GREEN{
        @Override
        public boolean isStart() {
            return true;
        }
    },
    END{
        @Override
        public boolean isStart() {
            return false;
        }
    };



    /**
     * 
     * @return ist dieser Typ ein Start-Typ
     */
    public abstract boolean isStart();
}
