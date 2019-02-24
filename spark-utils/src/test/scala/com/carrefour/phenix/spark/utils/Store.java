package com.carrefour.phenix.spark.utils;

public class Store extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ =
            new org.apache.avro.Schema.Parser().parse(
                    "{\"type\":\"record\",\"name\":\"Store\",\"namespace\":\"com.carrefour.phenix.spark.utils\",\"doc\":\"Store Pivot containing all information at stoEan level\",\"fields\":[" +
                            "{\"name\":\"stoEan\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store EAN GLN\",\"default\":null}," +
                            "{\"name\":\"stoAnabelKey\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store Anabel Key\",\"default\":null}," +
                            "{\"name\":\"weekPattern\",\"type\":[\"null\"," +
                            "{\"type\":\"record\",\"name\":\"WeekPattern\",\"namespace\":\"com.carrefour.phenix.spark.utils\",\"doc\":\"Pattern for the week\",\"fields\":[" +
                            "{\"name\":\"patternId\",\"type\":[\"null\",\"int\"],\"doc\":\"Identifier of the pattern\",\"default\":null}," +
                            "{\"name\":\"begDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Start date\",\"default\":null}," +
                            "{\"name\":\"endDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}]}" +
                            "]}]}]}");
    public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
    @Deprecated public java.lang.String stoEan;
    @Deprecated public java.lang.String stoAnabelKey;
    @Deprecated public com.carrefour.phenix.spark.utils.WeekPattern weekPattern;
    public Store() {}

    /**
     * All-args constructor.
     */
    public Store(java.lang.String stoEan, java.lang.String stoAnabelKey, com.carrefour.phenix.spark.utils.WeekPattern weekPattern) {
        this.stoEan = stoEan;
        this.stoAnabelKey = stoAnabelKey;
        this.weekPattern = weekPattern;
    }

    public org.apache.avro.Schema getSchema() { return SCHEMA$; }
    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0: return stoEan;
            case 1: return stoAnabelKey;
            case 2: return weekPattern;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0: stoEan = (java.lang.String)value$; break;
            case 1: stoAnabelKey = (java.lang.String)value$; break;
            case 2: weekPattern = (com.carrefour.phenix.spark.utils.WeekPattern)value$; break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public java.lang.String getStoEan() {
        return stoEan;
    }

    public void setStoEan(java.lang.String value) {
        this.stoEan = value;
    }

    public java.lang.String getStoAnabelKey() {
        return stoAnabelKey;
    }

    public void setStoAnabelKey(java.lang.String value) { this.stoAnabelKey = value; }

    public com.carrefour.phenix.spark.utils.WeekPattern getWeekPattern() {
        return weekPattern;
    }

    public void setWeekPattern(com.carrefour.phenix.spark.utils.WeekPattern value) {
        this.weekPattern = value;
    }

    public static com.carrefour.phenix.spark.utils.Store.Builder newBuilder() {
        return new com.carrefour.phenix.spark.utils.Store.Builder();
    }

    /** Creates a new Store RecordBuilder by copying an existing Builder */
    public static com.carrefour.phenix.spark.utils.Store.Builder newBuilder(com.carrefour.phenix.spark.utils.Store.Builder other) {
        return new com.carrefour.phenix.spark.utils.Store.Builder(other);
    }

    /** Creates a new Store RecordBuilder by copying an existing Store instance */
    public static com.carrefour.phenix.spark.utils.Store.Builder newBuilder(com.carrefour.phenix.spark.utils.Store other) {
        return new com.carrefour.phenix.spark.utils.Store.Builder(other);
    }

    /**
     * RecordBuilder for Store instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<com.carrefour.phenix.spark.utils.Store>
            implements org.apache.avro.data.RecordBuilder<com.carrefour.phenix.spark.utils.Store> {

        private java.lang.String stoEan;
        private java.lang.String stoAnabelKey;
        private com.carrefour.phenix.spark.utils.WeekPattern weekPattern;
        private Builder() {
            super(com.carrefour.phenix.spark.utils.Store.SCHEMA$);
        }

        /** Creates a Builder by copying an existing Builder */
        private Builder(com.carrefour.phenix.spark.utils.Store.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.stoEan)) {
                this.stoEan = data().deepCopy(fields()[0].schema(), other.stoEan);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.stoAnabelKey)) {
                this.stoAnabelKey = data().deepCopy(fields()[1].schema(), other.stoAnabelKey);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.weekPattern)) {
                this.weekPattern = data().deepCopy(fields()[2].schema(), other.weekPattern);
                fieldSetFlags()[2] = true;
            }
        }

        /** Creates a Builder by copying an existing Store instance */
        private Builder(com.carrefour.phenix.spark.utils.Store other) {
            super(com.carrefour.phenix.spark.utils.Store.SCHEMA$);
            if (isValidValue(fields()[0], other.stoEan)) {
                this.stoEan = data().deepCopy(fields()[0].schema(), other.stoEan);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.stoAnabelKey)) {
                this.stoAnabelKey = data().deepCopy(fields()[1].schema(), other.stoAnabelKey);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.weekPattern)) {
                this.weekPattern = data().deepCopy(fields()[2].schema(), other.weekPattern);
                fieldSetFlags()[1] = true;
            }
        }

        /** Gets the value of the 'stoEan' field */
        public java.lang.String getStoEan() {
            return stoEan;
        }

        /** Sets the value of the 'stoEan' field */
        public com.carrefour.phenix.spark.utils.Store.Builder setStoEan(java.lang.String value) {
            validate(fields()[0], value);
            this.stoEan = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /** Checks whether the 'stoEan' field has been set */
        public boolean hasStoEan() {
            return fieldSetFlags()[0];
        }

        /** Clears the value of the 'stoEan' field */
        public com.carrefour.phenix.spark.utils.Store.Builder clearStoEan() {
            stoEan = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /** Gets the value of the 'stoAnabelKey' field */
        public java.lang.String getStoAnabelKey() {
            return stoAnabelKey;
        }

        /** Sets the value of the 'stoAnabelKey' field */
        public com.carrefour.phenix.spark.utils.Store.Builder setStoAnabelKey(java.lang.String value) {
            validate(fields()[1], value);
            this.stoAnabelKey = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /** Checks whether the 'stoAnabelKey' field has been set */
        public boolean hasStoAnabelKey() {
            return fieldSetFlags()[1];
        }

        /** Clears the value of the 'stoAnabelKey' field */
        public com.carrefour.phenix.spark.utils.Store.Builder clearStoAnabelKey() {
            stoAnabelKey = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /** Gets the value of the 'weekPattern' field */
        public com.carrefour.phenix.spark.utils.WeekPattern getWeekPattern() {
            return weekPattern;
        }

        /** Sets the value of the 'weekPattern' field */
        public com.carrefour.phenix.spark.utils.Store.Builder setWeekPattern(com.carrefour.phenix.spark.utils.WeekPattern value) {
            validate(fields()[2], value);
            this.weekPattern = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /** Checks whether the 'weekPattern' field has been set */
        public boolean hasWeekPattern() {
            return fieldSetFlags()[2];
        }

        /** Clears the value of the 'weekPattern' field */
        public com.carrefour.phenix.spark.utils.Store.Builder clearWeekPattern() {
            weekPattern = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        public com.carrefour.phenix.spark.utils.Store build() {
            try {
                com.carrefour.phenix.spark.utils.Store record = new com.carrefour.phenix.spark.utils.Store();
                record.stoEan = fieldSetFlags()[0] ? this.stoEan : (java.lang.String) defaultValue(fields()[0]);
                record.stoAnabelKey = fieldSetFlags()[1] ? this.stoAnabelKey : (java.lang.String) defaultValue(fields()[1]);
                record.weekPattern = fieldSetFlags()[2] ? this.weekPattern : (com.carrefour.phenix.spark.utils.WeekPattern) defaultValue(fields()[2]);
               return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
