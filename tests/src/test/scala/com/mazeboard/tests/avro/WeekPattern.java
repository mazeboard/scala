package com.mazeboard.tests.avro;

public class WeekPattern extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"WeekPattern\",\"namespace\":\"com.mazeboard.tests.avro\",\"doc\":\"Pattern for the week\",\"fields\":[" +
                    "{\"name\":\"patternId\",\"type\":[\"null\",\"int\"],\"doc\":\"Identifier of the pattern\",\"default\":null}," +
                    "{\"name\":\"begDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Start date\",\"default\":null}," +
                    "{\"name\":\"endDate\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"End Date\",\"default\":null}]}");
    public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
    /** Identifier of the pattern */
    @Deprecated public Integer patternId;
    /** Start date */
    @Deprecated public String begDate;
    /** End Date */
    @Deprecated public String endDate;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public WeekPattern() {}

    /**
     * All-args constructor.
     */
    public WeekPattern(Integer patternId, String begDate, String endDate) {
        this.patternId = patternId;
        this.begDate = begDate;
        this.endDate = endDate;
    }

    public org.apache.avro.Schema getSchema() { return SCHEMA$; }
    // Used by DatumWriter.  Applications should not call.
    public Object get(int field$) {
        switch (field$) {
            case 0: return patternId;
            case 1: return begDate;
            case 2: return endDate;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }
    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value="unchecked")
    public void put(int field$, Object value$) {
        switch (field$) {
            case 0: patternId = (Integer)value$; break;
            case 1: begDate = (String)value$; break;
            case 2: endDate = (String)value$; break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'patternId' field.
     * Identifier of the pattern   */
    public Integer getPatternId() {
        return patternId;
    }

    /**
     * Sets the value of the 'patternId' field.
     * Identifier of the pattern   * @param value the value to set.
     */
    public void setPatternId(Integer value) {
        this.patternId = value;
    }

    /**
     * Gets the value of the 'begDate' field.
     * Start date   */
    public String getBegDate() {
        return begDate;
    }

    /**
     * Sets the value of the 'begDate' field.
     * Start date   * @param value the value to set.
     */
    public void setBegDate(String value) {
        this.begDate = value;
    }

    /**
     * Gets the value of the 'endDate' field.
     * End Date   */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the 'endDate' field.
     * End Date   * @param value the value to set.
     */
    public void setEndDate(String value) {
        this.endDate = value;
    }

    /** Creates a new WeekPattern RecordBuilder */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Creates a new WeekPattern RecordBuilder by copying an existing Builder */
    public static Builder newBuilder(Builder other) {
        return new Builder(other);
    }

    /** Creates a new WeekPattern RecordBuilder by copying an existing WeekPattern instance */
    public static Builder newBuilder(WeekPattern other) {
        return new Builder(other);
    }

    /**
     * RecordBuilder for WeekPattern instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<WeekPattern>
            implements org.apache.avro.data.RecordBuilder<WeekPattern> {

        private Integer patternId;
        private String begDate;
        private String endDate;

        /** Creates a new Builder */
        private Builder() {
            super(WeekPattern.SCHEMA$);
        }

        /** Creates a Builder by copying an existing Builder */
        private Builder(Builder other) {
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
        private Builder(WeekPattern other) {
            super(WeekPattern.SCHEMA$);
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
        public Integer getPatternId() {
            return patternId;
        }

        /** Sets the value of the 'patternId' field */
        public Builder setPatternId(Integer value) {
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
        public Builder clearPatternId() {
            patternId = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /** Gets the value of the 'begDate' field */
        public String getBegDate() {
            return begDate;
        }

        /** Sets the value of the 'begDate' field */
        public Builder setBegDate(String value) {
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
        public Builder clearBegDate() {
            begDate = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /** Gets the value of the 'endDate' field */
        public String getEndDate() {
            return endDate;
        }

        /** Sets the value of the 'endDate' field */
        public Builder setEndDate(String value) {
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
        public Builder clearEndDate() {
            endDate = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        public WeekPattern build() {
            try {
                WeekPattern record = new WeekPattern();
                record.patternId = fieldSetFlags()[0] ? this.patternId : (Integer) defaultValue(fields()[0]);
                record.begDate = fieldSetFlags()[1] ? this.begDate : (String) defaultValue(fields()[1]);
                record.endDate = fieldSetFlags()[2] ? this.endDate : (String) defaultValue(fields()[2]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
