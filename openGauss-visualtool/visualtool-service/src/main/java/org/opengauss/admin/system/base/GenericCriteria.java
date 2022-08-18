/**
 Copyright (c) 2014-2016 abel533@gmail.com.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.system.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericCriteria {

    protected boolean distinct;

    public String orderByClause;

    protected String tableCate;

    private Integer firstResult;
    private Integer maxResults;

    private Map<String, Object> params;

    protected List<Criteria> generic;

    public GenericCriteria() {
        generic = new ArrayList<Criteria>();
        params = new HashMap<String, Object>();
    }

    public void or(Criteria criteria) {
        generic.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        generic.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (generic.size() == 0) {
            generic.add(criteria);
        }
        return criteria;
    }

    public void put(String key, Object value) {
        params.put(key, value);
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        generic.clear();
        params.clear();
        orderByClause = null;
        distinct = false;
    }

    public List<Criteria> getGeneric() {
        return generic;
    }

    public void setGeneric(List<Criteria> generic) {
        this.generic = generic;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public String getTableCate() {
        return tableCate;
    }

    public void setTableCate(String tableCate) {
        this.tableCate = tableCate;
    }

    protected abstract static class AbstractGeneratedCriteria {
        protected List<Criterion> criteria;

        protected AbstractGeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value,
                                    String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property
                        + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1,
                                    Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property
                        + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria condition(String property) {
            addCriterion(property);
            return (Criteria) this;
        }

        public Criteria isNull(String property) {
            addCriterion(property + " is null");
            return (Criteria) this;
        }

        public Criteria isNotNull(String property) {
            addCriterion(property + " is not null");
            return (Criteria) this;
        }

        public Criteria equalTo(String property, Object value) {
            addCriterion(property + " =", value, property);
            return (Criteria) this;
        }

        public Criteria notEqualTo(String property, Object value) {
            addCriterion(property + " <>", value, property);
            return (Criteria) this;
        }

        public Criteria greaterThan(String property, Object value) {
            addCriterion(property + " >", value, property);
            return (Criteria) this;
        }

        public Criteria greaterThanOrEqualTo(String property, Object value) {
            addCriterion(property + " >=", value, property);
            return (Criteria) this;
        }

        public Criteria lessThan(String property, Object value) {
            addCriterion(property + " <", value, property);
            return (Criteria) this;
        }

        public Criteria lessThanOrEqualTo(String property, Object value) {
            addCriterion(property + " <=", value, property);
            return (Criteria) this;
        }

        public Criteria like(String property, Object value) {
            addCriterion(property + " like", value, property);
            return (Criteria) this;
        }

        public Criteria notLike(String property, Object value) {
            addCriterion(property + " not like", value, property);
            return (Criteria) this;
        }

        public Criteria in(String property, List<Object> values) {
            addCriterion(property + " in", values, property);
            return (Criteria) this;
        }

        public Criteria notIn(String property, List<Object> values) {
            addCriterion(property + " not in", values, property);
            return (Criteria) this;
        }

        public Criteria between(String property, Object value1, Object value2) {
            addCriterion(property + " between", value1, value2, property);
            return (Criteria) this;
        }

        public Criteria notBetween(String property, Object value1, Object value2) {
            addCriterion(property + " between", value1, value2, property);
            return (Criteria) this;
        }

        public Criteria exists(String sql) {
            addCriterion(sql);
            return (Criteria) this;
        }

        public Criteria sinSql(String sql) {
            addCriterion(sql);
            return (Criteria) this;
        }

    }

    public static class Criteria extends AbstractGeneratedCriteria {

        protected Criteria() {
            super();
        }

        public Criteria likeInsensitive(String property, String value) {
            addCriterion("upper(" + property + ") like", value.toUpperCase(),
                    property);
            return this;
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue,
                            String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}