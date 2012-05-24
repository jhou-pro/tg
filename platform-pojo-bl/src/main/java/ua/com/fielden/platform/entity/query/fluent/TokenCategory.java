package ua.com.fielden.platform.entity.query.fluent;

public enum TokenCategory {
    PROP, EXT_PROP, VAL, IVAL, PARAM, IPARAM, EXPR, EXPR_TOKENS, EQUERY_TOKENS, //
    ZERO_ARG_FUNCTION, //
    //SINGLE_OPERAND, //
    ANY_OF_PROPS, ANY_OF_VALUES, ANY_OF_PARAMS, ANY_OF_EXPR_TOKENS, ANY_OF_EQUERY_TOKENS,
    ALL_OF_PROPS, ALL_OF_VALUES, ALL_OF_PARAMS, ALL_OF_EXPR_TOKENS, ALL_OF_EQUERY_TOKENS,
    SET_OF_PROPS, SET_OF_VALUES, SET_OF_PARAMS, SET_OF_EXPR_TOKENS,

    COMPARISON_OPERATOR, NULL_OPERATOR, EXISTS_OPERATOR, LIKE_OPERATOR, ILIKE_OPERATOR, IN_OPERATOR, ANY_OPERATOR, ALL_OPERATOR,
    //TODO replace ANY_OPERATOR, ALL_OPERATOR with QUANTIFIED_OPERATOR and use SqlTokens.ALL/ANY in addition
    LOGICAL_OPERATOR,
    ARITHMETICAL_OPERATOR, //

   /**/CONDITION/**/, /*COMPARISON_TEST, NULL_TEST, EXISTENCE_TEST, LIKE_TEST, SET_TEST, QUANTIFIED_TEST,*/ GROUPED_CONDITIONS, /*CONDITIONS,*/ //

    BEGIN_EXPR, END_EXPR, BEGIN_COND, END_COND,

    SORT_ORDER,

    AS_ALIAS,
    END_FUNCTION,
    ENTITY_ALIAS,
    FUNCTION, COLLECTIONAL_FUNCTION,
    QUERY_TOKEN,
    QRY_SOURCE_ALIAS,
    ENTITY_TYPE_AS_QRY_SOURCE,
    QRY_MODELS_AS_QRY_SOURCE,
    QRY_SOURCE, QRY_COMPOUND_SOURCE,

    COMPOUND_CONDITION, FUNCTION_MODEL, ON, JOIN_TYPE,
    QRY_YIELD, QRY_GROUP
    ;
}
