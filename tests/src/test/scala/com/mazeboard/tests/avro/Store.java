package com.mazeboard.tests.avro;

public class Store extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ =
            new org.apache.avro.Schema.Parser().parse(
                    "{\"type\":\"record\",\"name\":\"Store\",\"namespace\":\"com.mazeboard.tests.avro\",\"doc\":\"Store Pivot containing all information at stoEan level\",\"fields\":[" +
                            "{\"name\":\"stoEan\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store EAN GLN\",\"default\":null}," +
                            "{\"name\":\"stoAnabelKey\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Store Anabel Key\",\"default\":null}," +
                            "{\"name\":\"weekPattern\",\"type\":[\"null\"," +
                            "{\"type\":\"record\",\"name\":\"WeekPattern\",\"namespace\":\"com.mazeboard.tests.avro\",\"doc\":\"Pattern for the week\",\"fields\":[" +
                            "{\"name\":\"patternId\",\"type\":[\"null\",\"int\"],\"doc\":\"Identifier of the pattern\",\"default\":null}," +
                            "{\"name\":\"begDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Start date\",\"default\":null}," +
                            "{\"name\":\"endDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}]}" +
                            "]}]}]}");
    public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
    @Deprecated public String stoEan;
    @Deprecated public String stoAnabelKey;
    @Deprecated public WeekPattern weekPattern;
    public Store() {}

    /**
     * All-args constructor.
     */
    public Store(String stoEan, String stoAnabelKey, WeekPattern weekPattern) {
        this.stoEan = stoEan;
        this.stoAnabelKey = stoAnabelKey;
        this.weekPattern = weekPattern;
    }

    public org.apache.avro.Schema getSchema() { return SCHEMA$; }
    public Object get(int field$) {
        switch (field$) {
            case 0: return stoEan;
            case 1: return stoAnabelKey;
            case 2: return weekPattern;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0: stoEan = (String)value$; break;
            case 1: stoAnabelKey = (String)value$; break;
            case 2: weekPattern = (WeekPattern)value$; break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public String getStoEan() {
        return stoEan;
    }

    public void setStoEan(String value) {
        this.stoEan = value;
    }

    public String getStoAnabelKey() {
        return stoAnabelKey;
    }

    public void setStoAnabelKey(String value) { this.stoAnabelKey = value; }

    public WeekPattern getWeekPattern() {
        return weekPattern;
    }

    public void setWeekPattern(WeekPattern value) {
        this.weekPattern = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /** Creates a new Store RecordBuilder by copying an existing Builder */
    public static Builder newBuilder(Builder other) {
        return new Builder(other);
    }

    /** Creates a new Store RecordBuilder by copying an existing Store instance */
    public static Builder newBuilder(Store other) {
        return new Builder(other);
    }

    /**
     * RecordBuilder for Store instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Store>
            implements org.apache.avro.data.RecordBuilder<Store> {

        private String stoEan;
        private String stoAnabelKey;
        private WeekPattern weekPattern;
        private Builder() {
            super(Store.SCHEMA$);
        }

        /** Creates a Builder by copying an existing Builder */
        private Builder(Builder other) {
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
        private Builder(Store other) {
            super(Store.SCHEMA$);
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
        public String getStoEan() {
            return stoEan;
        }

        /** Sets the value of the 'stoEan' field */
        public Builder setStoEan(String value) {
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
        public Builder clearStoEan() {
            stoEan = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /** Gets the value of the 'stoAnabelKey' field */
        public String getStoAnabelKey() {
            return stoAnabelKey;
        }

        /** Sets the value of the 'stoAnabelKey' field */
        public Builder setStoAnabelKey(String value) {
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
        public Builder clearStoAnabelKey() {
            stoAnabelKey = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /** Gets the value of the 'weekPattern' field */
        public WeekPattern getWeekPattern() {
            return weekPattern;
        }

        /** Sets the value of the 'weekPattern' field */
        public Builder setWeekPattern(WeekPattern value) {
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
        public Builder clearWeekPattern() {
            weekPattern = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        public Store build() {
            try {
                Store record = new Store();
                record.stoEan = fieldSetFlags()[0] ? this.stoEan : (String) defaultValue(fields()[0]);
                record.stoAnabelKey = fieldSetFlags()[1] ? this.stoAnabelKey : (String) defaultValue(fields()[1]);
                record.weekPattern = fieldSetFlags()[2] ? this.weekPattern : (WeekPattern) defaultValue(fields()[2]);
               return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
