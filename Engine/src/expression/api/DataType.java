package expression.api;

public enum DataType {

    NUMERIC{
      public Object cast(Object value) {
          return (double)value;
      }
    },
    STRING{
        public Object cast(Object value) {
            return (String)value;
        }
    },
    BOOLEAN{
        public Object cast(Object value) {
            return (boolean)value;
        }
    },
    UNKNOWN{
        public Object cast(Object value) {
            return value;
        }
    };

    public abstract Object cast(Object obj);
}
