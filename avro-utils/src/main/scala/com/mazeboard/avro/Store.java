package com.mazeboard.avro;

import org.apache.spark.sql.catalyst.DefinedByConstructorParams;
import java.io.Serializable;

public class Store extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord, DefinedByConstructorParams, Serializable {
    public static final org.apache.avro.Schema SCHEMA$ =
            new org.apache.avro.Schema.Parser().parse(
                    "{\"type\":\"record\",\"name\":\"Store\",\"namespace\":\"com.mazeboard.avro\",\"doc\":\"Store Pivot containing all information at stoEan level\",\"fields\":[" +
                            "{\"name\":\"stoEan\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store EAN GLN\",\"default\":null}," +
                            "{\"name\":\"stoAnabelKey\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store Anabel Key\",\"default\":null}," +
                            "{\"name\":\"weekPattern\",\"type\":[\"null\"," +
                            "{\"type\":\"record\",\"name\":\"WeekPattern\",\"namespace\":\"com.mazeboard.avro\",\"doc\":\"Pattern for the week\",\"fields\":[" +
                            "{\"name\":\"patternId\",\"type\":[\"null\",\"int\"],\"doc\":\"Identifier of the pattern\",\"default\":null}," +
                            "{\"name\":\"begDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Start date\",\"default\":null}," +
                            "{\"name\":\"endDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}]}" +
                            "]}]}]}");
    public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
     public java.lang.String stoEan;
     public java.lang.String stoAnabelKey;
     public com.mazeboard.avro.WeekPattern weekPattern;

    public Store(java.lang.String stoEan, java.lang.String stoAnabelKey, com.mazeboard.avro.WeekPattern weekPattern) {
        this.stoEan = stoEan;
        this.stoAnabelKey = stoAnabelKey;
        this.weekPattern = weekPattern;
    }

    //public Store() {}

    public java.lang.String stoEan() { return this.stoEan; }
    public java.lang.String stoAnabelKey() { return this.stoAnabelKey; }
    public com.mazeboard.avro.WeekPattern weekPattern() {
        return this.weekPattern;
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
            case 2: weekPattern = (com.mazeboard.avro.WeekPattern)value$; break;
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

    public com.mazeboard.avro.WeekPattern getWeekPattern() {
        return weekPattern;
    }

    public void setWeekPattern(com.mazeboard.avro.WeekPattern value) {
        this.weekPattern = value;
    }

    public static com.mazeboard.avro.Store.Builder newBuilder() {
        return new com.mazeboard.avro.Store.Builder();
    }

    /** Creates a new Store RecordBuilder by copying an existing Builder */
    public static com.mazeboard.avro.Store.Builder newBuilder(com.mazeboard.avro.Store.Builder other) {
        return new com.mazeboard.avro.Store.Builder(other);
    }

    /** Creates a new Store RecordBuilder by copying an existing Store instance */
    public static com.mazeboard.avro.Store.Builder newBuilder(com.mazeboard.avro.Store other) {
        return new com.mazeboard.avro.Store.Builder(other);
    }

    /**
     * RecordBuilder for Store instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<com.mazeboard.avro.Store>
            implements org.apache.avro.data.RecordBuilder<com.mazeboard.avro.Store> {

        private java.lang.String stoEan;
        private java.lang.String stoAnabelKey;
        private com.mazeboard.avro.WeekPattern weekPattern;
        private Builder() {
            super(com.mazeboard.avro.Store.SCHEMA$);
        }

        /** Creates a Builder by copying an existing Builder */
        private Builder(com.mazeboard.avro.Store.Builder other) {
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
        private Builder(com.mazeboard.avro.Store other) {
            super(com.mazeboard.avro.Store.SCHEMA$);
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
        public com.mazeboard.avro.Store.Builder setStoEan(java.lang.String value) {
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
        public com.mazeboard.avro.Store.Builder clearStoEan() {
            stoEan = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /** Gets the value of the 'stoAnabelKey' field */
        public java.lang.String getStoAnabelKey() {
            return stoAnabelKey;
        }

        /** Sets the value of the 'stoAnabelKey' field */
        public com.mazeboard.avro.Store.Builder setStoAnabelKey(java.lang.String value) {
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
        public com.mazeboard.avro.Store.Builder clearStoAnabelKey() {
            stoAnabelKey = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /** Gets the value of the 'weekPattern' field */
        public com.mazeboard.avro.WeekPattern getWeekPattern() {
            return weekPattern;
        }

        /** Sets the value of the 'weekPattern' field */
        public com.mazeboard.avro.Store.Builder setWeekPattern(com.mazeboard.avro.WeekPattern value) {
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
        public com.mazeboard.avro.Store.Builder clearWeekPattern() {
            weekPattern = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        public com.mazeboard.avro.Store build() {
            try {
                return new com.mazeboard.avro.Store(fieldSetFlags()[0] ? this.stoEan : (java.lang.String) defaultValue(fields()[0]),
                        fieldSetFlags()[1] ? this.stoAnabelKey : (java.lang.String) defaultValue(fields()[1]),
                        fieldSetFlags()[2] ? this.weekPattern : (com.mazeboard.avro.WeekPattern) defaultValue(fields()[2]));
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
        /*@Override
        public com.mazeboard.avro.Store build() {
            try {
                com.mazeboard.avro.Store record = new com.mazeboard.avro.Store();
                record.stoEan = fieldSetFlags()[0] ? this.stoEan : (java.lang.String) defaultValue(fields()[0]);
                record.stoAnabelKey = fieldSetFlags()[1] ? this.stoAnabelKey : (java.lang.String) defaultValue(fields()[1]);
                record.weekPattern = fieldSetFlags()[2] ? this.weekPattern : (com.mazeboard.avro.WeekPattern) defaultValue(fields()[2]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }*/
    }
}
