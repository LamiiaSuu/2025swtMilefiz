package de.hs_rm.de.milefiz.game.model;

public enum FieldType {
    NORMAL{
        @Override
        public boolean isStart() {
            return false;
        }

        @Override
        public boolean isEnd() {
           return false;
        }
    }, 
    START_RED{
        @Override
        public boolean isStart() {
            return true;
        }

       @Override
        public boolean isEnd() {
           return false;
        }       
    },
    START_YELLOW{
        @Override
        public boolean isStart() {
            return true;
        }

        @Override
        public boolean isEnd() {
           return false;
        }      
    },
    START_BLUE{
        @Override
        public boolean isStart() {
            return true;
        }

        @Override
        public boolean isEnd() {
           return false;
        }
    },
    START_GREEN{
        @Override
        public boolean isStart() {
            return true;
        }

       @Override
        public boolean isEnd() {
           return false;
        }
    },
    END{
        @Override
        public boolean isStart() {
            return false;
        }
        @Override
        public boolean isEnd() {
           return true;
        }
    };



    /**
     * 
     * @return ist dieser Typ ein Start-Typ
     */
    public abstract boolean isStart();

    /**
     * 
     * @return ist dieser Typ ein Ziel
     */
    public abstract boolean isEnd();
}
