/**
  *     https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.mat.ui.help%2Freference%2Foqlsyntax.html
**/
grammar OQL;

@header {
package com.github.ol_loginov.heaplibweb.oql;
}

selectStatement
    : 'SELECT' selectList fromClause ( whereClause )? ( unionClause )?
    ;

selectList
    : (( 'DISTINCT' | 'AS RETAINED SET' )? ( '*' | 'OBJECTS' selectItem | selectItem ( ',' selectItem )* ))
    ;

selectItem
    : ( pathExpression | envVarPathExpression ) ( 'AS' ( STRING_LITERAL | IDENTIFIER ) )?
    ;

pathExpression
    : ( objectFacet | builtInFunction ) ( '.' objectFacet | '[' simpleExpression ( ':' simpleExpression)? ']' )*
    ;
    
envVarPathExpression
    : ( '$' '{' IDENTIFIER '}' ) ( '.' objectFacet | '[' simpleExpression ( ':' simpleExpression)? ']' )*
    ;
    
objectFacet 
    : ( ( '@' )? IDENTIFIER ( parameterList )? )
    ;
    
parameterList
    : '(' ( ( simpleExpression ( ',' simpleExpression )* ) )? ')'
    ;

fromClause
    : 'FROM' ( 'OBJECTS' )? ( 'INSTANCEOF' )? ( fromItem | '(' selectStatement ')' ) ( IDENTIFIER )?
    ;
fromItem
    : ( className | STRING_LITERAL | objectAddress ( ',' objectAddress )* | objectId ( ',' objectId )* | envVarPathExpression )
    ;
className
    : ( IDENTIFIER ( '.' IDENTIFIER )* ( '[]' )* )
    ;
objectAddress
    : HEX_LITERAL
    ;
objectId
    : INTEGER_LITERAL
    ;
whereClause
    : 'WHERE' conditionalOrExpression
    ;
conditionalOrExpression
    : conditionalAndExpression ( 'or' conditionalAndExpression )*
    ;
conditionalAndExpression
    : equalityExpression ( 'and' equalityExpression )*
    ;
equalityExpression
    : relationalExpression ( ( '=' relationalExpression | '!=' relationalExpression ) )*
    ;
relationalExpression
    : ( simpleExpression ( ( '<' simpleExpression | '>' simpleExpression | '<=' simpleExpression | '>=' simpleExpression | ( likeClause | inClause ) | 'implements' className ) )? )
    ;
likeClause
    : ( 'NOT' )? 'LIKE' STRING_LITERAL
    ;
inClause
    : ( 'NOT' )? 'IN' simpleExpression
    ;
simpleExpression
    : multiplicativeExpression ( '+' multiplicativeExpression | '-' multiplicativeExpression )*
    ;
multiplicativeExpression
    : primaryExpression ( '*' primaryExpression | '/' primaryExpression )*
    ;
primaryExpression
    : literal | '(' ( conditionalOrExpression | subQuery ) ')' | pathExpression  | envVarPathExpression
    ;
subQuery
    : selectStatement
    ;
builtInFunction
    : ( ( 'toHex' | 'toString' | 'dominators' | 'outbounds' | 'inbounds' | 'classof' | 'dominatorof' ) '(' conditionalOrExpression ')' )
    ;
literal
    : ( INTEGER_LITERAL | LONG_LITERAL | FLOATING_POINT_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | booleanLiteral | nullLiteral )
    ;
booleanLiteral
    : 'true' | 'false'
    ;
nullLiteral
    : NULL
    ;
unionClause
    : ( 'UNION' '(' selectStatement ')' )+
    ;

NULL
    : 'null'
    ;
STRING_LITERAL
	:	'"' StringCharacters? '"'
	;

CHARACTER_LITERAL
    : ['] StringCharacter [']
    ;

fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["\\]
	|	EscapeSequence
	;
fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
	|	OctalEscape
    |   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
	;
fragment
OctalEscape
	:	'\\' OctalDigit
	|	'\\' OctalDigit OctalDigit
	|	'\\' ZeroToThree OctalDigit OctalDigit
	;
fragment
OctalDigits
	:	OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
	;
fragment
OctalDigit
	:	[0-7]
	;
fragment
OctalDigitsAndUnderscores
	:	OctalDigitOrUnderscore+
	;

fragment
OctalDigitOrUnderscore
	:	OctalDigit
	|	'_'
	;
fragment
ZeroToThree
	:	[0-3]
	;
fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;
fragment
HexDigit
	:	[0-9a-fA-F]
	;

fragment
DIGITS_LITERAL: [0-9]+ ;

INTEGER_LITERAL:	'-'? DIGITS_LITERAL;

LONG_LITERAL:	INTEGER_LITERAL 'L';

FLOATING_POINT_LITERAL
    : '-'? DIGITS_LITERAL '.' DIGITS_LITERAL
    ;

HEX_LITERAL: '0x' DIGITS_LITERAL;

IDENTIFIER
	:	JavaLetter JavaLetterOrDigit*
	;
fragment
JavaLetter
	:	[a-zA-Z$_] // these are the "java letters" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
JavaLetterOrDigit
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;
