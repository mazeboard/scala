package com.carrefour.phenix.spark.utils;

public class WeekPattern extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"WeekPattern\",\"namespace\":\"com.carrefour.phenix.spark.utils\",\"doc\":\"Pattern for the week\",\"fields\":[" +
                    "{\"name\":\"patternId\",\"type\":[\"null\",\"int\"],\"doc\":\"Identifier of the pattern\",\"default\":null}," +
                    "{\"name\":\"begDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Start date\",\"default\":null}," +
                    "{\"name\":\"endDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"End Date\",\"default\":null}]}");
    public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
    /** Identifier of the pattern */
    @Deprecated public java.lang.Integer patternId;
    /** Start date */
    @Deprecated public java.lang.String begDate;
    /** End Date */
    @Deprecated public java.lang.String endDate;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public WeekPattern() {}

    /**
     * All-args constructor.
     */
    public WeekPattern(java.lang.Integer patternId, java.lang.String begDate, java.lang.String endDate) {
        this.patternId = patternId;
        this.begDate = begDate;
        this.endDate = endDate;
    }

    public org.apache.avro.Schema getSchema() { return SCHEMA$; }
    // Used by DatumWriter.  Applications should not call.
    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0: return patternId;
            case 1: return begDate;
            case 2: return endDate;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }
    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value="unchecked")
    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0: patternId = (java.lang.Integer)value$; break;
            case 1: begDate = (java.lang.String)value$; break;
            case 2: endDate = (java.lang.String)value$; break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'patternId' field.
     * Identifier of the pattern   */
    public java.lang.Integer getPatternId() {
        return patternId;
    }

    /**
     * Sets the value of the 'patternId' field.
     * Identifier of the pattern   * @param value the value to set.
     */
    public void setPatternId(java.lang.Integer value) {
        this.patternId = value;
    }

    /**
     * Gets the value of the 'begDate' field.
     * Start date   */
    public java.lang.String getBegDate() {
        return begDate;
    }

    /**
     * Sets the value of the 'begDate' field.
     * Start date   * @param value the value to set.
     */
    public void setBegDate(java.lang.String value) {
        this.begDate = value;
    }

    /**
     * Gets the value of the 'endDate' field.
     * End Date   */
    public java.lang.String getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the 'endDate' field.
     * End Date   * @param value the value to set.
     */
    public void setEndDate(java.lang.String value) {
        this.endDate = value;
    }

    /** Creates a new WeekPattern RecordBuilder */
    public static com.carrefour.phenix.spark.utils.WeekPattern.Builder newBuilder() {
        return new com.carrefour.phenix.spark.utils.WeekPattern.Builder();
    }

    /** Creates a new WeekPattern RecordBuilder by copying an existing Builder */
    public static com.carrefour.phenix.spark.utils.WeekPattern.Builder newBuilder(com.carrefour.phenix.spark.utils.WeekPattern.Builder other) {
        return new com.carrefour.phenix.spark.utils.WeekPattern.Builder(other);
    }

    /** Creates a new WeekPattern RecordBuilder by copying an existing WeekPattern instance */
    public static com.carrefour.phenix.spark.utils.WeekPattern.Builder newBuilder(com.carrefour.phenix.spark.utils.WeekPattern other) {
        return new com.carrefour.phenix.spark.utils.WeekPattern.Builder(other);
    }

    /**
     * RecordBuilder for WeekPattern instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<WeekPattern>
            implements org.apache.avro.data.RecordBuilder<WeekPattern> {

        private java.lang.Integer patternId;
        private java.lang.String begDate;
        private java.lang.String endDate;

        /** Creates a new Builder */
        private Builder() {
            super(com.carrefour.phenix.spark.utils.WeekPattern.SCHEMA$);
        }

        /** Creates a Builder by copying an existing Builder */
        private Builder(com.carrefour.phenix.spark.utils.WeekPattern.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.patternId)) {
                this.patternId = data().deepCopy(fields()[0].schema(), other.patternId);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.begDate)) {
                this.begDate = data().deepCopy(fields()[1].schema(), other.begDate);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.endDate)) {
                this.endDate = data().deepCopy(fields()[2].schema(), other.endDate);
                fieldSetFlags()[2] = true;
            }
        }

        /** Creates a Builder by copying an existing WeekPattern instance */
        private Builder(com.carrefour.phenix.spark.utils.WeekPattern other) {
            super(com.carrefour.phenix.spark.utils.WeekPattern.SCHEMA$);
            if (isValidValue(fields()[0], other.patternId)) {
                this.patternId = data().deepCopy(fields()[0].schema(), other.patternId);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.begDate)) {
                this.begDate = data().deepCopy(fields()[1].schema(), other.begDate);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.endDate)) {
                this.endDate = data().deepCopy(fields()[2].schema(), other.endDate);
                fieldSetFlags()[2] = true;
            }
        }

        /** Gets the value of the 'patternId' field */
        public java.lang.Integer getPatternId() {
            return patternId;
        }

        /** Sets the value of the 'patternId' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder setPatternId(java.lang.Integer value) {
            validate(fields()[0], value);
            this.patternId = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /** Checks whether the 'patternId' field has been set */
        public boolean hasPatternId() {
            return fieldSetFlags()[0];
        }

        /** Clears the value of the 'patternId' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder clearPatternId() {
            patternId = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /** Gets the value of the 'begDate' field */
        public java.lang.String getBegDate() {
            return begDate;
        }

        /** Sets the value of the 'begDate' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder setBegDate(java.lang.String value) {
            validate(fields()[1], value);
            this.begDate = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /** Checks whether the 'begDate' field has been set */
        public boolean hasBegDate() {
            return fieldSetFlags()[1];
        }

        /** Clears the value of the 'begDate' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder clearBegDate() {
            begDate = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /** Gets the value of the 'endDate' field */
        public java.lang.String getEndDate() {
            return endDate;
        }

        /** Sets the value of the 'endDate' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder setEndDate(java.lang.String value) {
            validate(fields()[2], value);
            this.endDate = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /** Checks whether the 'endDate' field has been set */
        public boolean hasEndDate() {
            return fieldSetFlags()[2];
        }

        /** Clears the value of the 'endDate' field */
        public com.carrefour.phenix.spark.utils.WeekPattern.Builder clearEndDate() {
            endDate = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        public WeekPattern build() {
            try {
                WeekPattern record = new WeekPattern();
                record.patternId = fieldSetFlags()[0] ? this.patternId : (java.lang.Integer) defaultValue(fields()[0]);
                record.begDate = fieldSetFlags()[1] ? this.begDate : (java.lang.String) defaultValue(fields()[1]);
                record.endDate = fieldSetFlags()[2] ? this.endDate : (java.lang.String) defaultValue(fields()[2]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
