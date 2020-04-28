grammar SQL;

parse :
    sql_stmt_list ;

sql_stmt_list :
    ';'* sql_stmt ( ';'+ sql_stmt )* ';'* ;

sql_stmt :
    create_table_stmt
    | create_db_stmt
    | create_user_stmt
    | drop_db_stmt
    | drop_user_stmt
    | delete_stmt
    | drop_table_stmt
    | insert_stmt
    | select_stmt
    | create_view_stmt
    | drop_view_stmt
    | grant_stmt
    | revoke_stmt
    | use_db_stmt
    | show_db_stmt
    | show_table_stmt
    | show_meta_stmt
    | quit_stmt
    | update_stmt ;

create_db_stmt :
    K_CREATE K_DATABASE database_name ;

drop_db_stmt :
    K_DROP K_DATABASE ( K_IF K_EXISTS )? database_name ;

create_user_stmt :
    K_CREATE K_USER user_name K_IDENTIFIED K_BY password ;

drop_user_stmt :
    K_DROP K_USER ( K_IF K_EXISTS )? user_name ;

create_table_stmt :
    K_CREATE K_TABLE table_name
        '(' column_def ( ',' column_def )* ( ',' table_constraint )? ')' ;

show_meta_stmt :
    K_SHOW K_TABLE table_name ;

grant_stmt :
    K_GRANT auth_level ( ',' auth_level )* K_ON table_name K_TO user_name ;

revoke_stmt :
    K_REVOKE auth_level ( ',' auth_level )* K_ON table_name K_FROM user_name ;

use_db_stmt :
    K_USE database_name;

delete_stmt :
    K_DELETE K_FROM table_name ( K_WHERE multiple_condition )? ;

drop_table_stmt :
    K_DROP K_TABLE ( K_IF K_EXISTS )? table_name ;

show_db_stmt :
    K_SHOW K_DATABASES;

quit_stmt :
    K_QUIT;

show_table_stmt :
    K_SHOW K_DATABASE database_name;

insert_stmt :
    K_INSERT K_INTO table_name ( '(' column_name ( ',' column_name )* ')' )?
        K_VALUES value_entry ( ',' value_entry )* ;

value_entry :
    '(' literal_value ( ',' literal_value )* ')' ;

select_stmt :
    K_SELECT ( K_DISTINCT | K_ALL )? result_column ( ',' result_column )*
        K_FROM table_query ( ',' table_query )* ( K_WHERE multiple_condition )? ;

create_view_stmt :
    K_CREATE K_VIEW view_name K_AS select_stmt ;

drop_view_stmt :
    K_DROP K_VIEW ( K_IF K_EXISTS )? view_name ;

update_stmt :
    K_UPDATE table_name
        K_SET column_name '=' expression ( K_WHERE multiple_condition )? ;

column_def :
    column_name type_name column_constraint* ;

type_name :
    T_INT
    | T_LONG
    | T_FLOAT
    | T_DOUBLE
    | T_STRING '(' NUMERIC_LITERAL ')' ;

column_constraint :
    K_PRIMARY K_KEY
    | K_NOT K_NULL ;

multiple_condition :
    condition
    | multiple_condition AND multiple_condition
    | multiple_condition OR multiple_condition ;

condition :
    expression comparator expression;

comparer :
    column_full_name
    | literal_value ;

comparator :
    EQ | NE | LE | GE | LT | GT ;

expression :
    comparer
    | expression ( MUL | DIV ) expression
    | expression ( ADD | SUB ) expression
    | '(' expression ')';

table_constraint :
    K_PRIMARY K_KEY '(' column_name (',' column_name)* ')' ;

result_column
    : '*'
    | table_name '.' '*'
    | column_full_name;

table_query :
    table_name
    | table_name ( K_JOIN table_name )+ K_ON multiple_condition ;

auth_level :
    K_SELECT | K_INSERT | K_UPDATE | K_DELETE | K_DROP ;

literal_value :
    NUMERIC_LITERAL
    | STRING_LITERAL
    | K_NULL ;

column_full_name:
    ( table_name '.' )? column_name ;

database_name :
    IDENTIFIER ;

table_name :
    IDENTIFIER ;

user_name :
    IDENTIFIER ;

column_name :
    IDENTIFIER ;

view_name :
    IDENTIFIER;

password :
    STRING_LITERAL ;

EQ : '=';
NE : '<>';
LT : '<';
GT : '>';
LE : '<=';
GE : '>=';

ADD : '+';
SUB : '-';
MUL : '*';
DIV : '/';

AND : '&&';
OR : '||';

T_INT : I N T;
T_LONG : L O N G;
T_FLOAT : F L O A T;
T_DOUBLE : D O U B L E;
T_STRING : S T R I N G;

K_ADD : A D D;
K_ALL : A L L;
K_AS : A S;
K_BY : B Y;
K_COLUMN : C O L U M N;
K_CREATE : C R E A T E;
K_DATABASE : D A T A B A S E;
K_DATABASES : D A T A B A S E S;
K_DELETE : D E L E T E;
K_DISTINCT : D I S T I N C T;
K_DROP : D R O P;
K_EXISTS : E X I S T S;
K_FROM : F R O M;
K_GRANT : G R A N T;
K_IF : I F;
K_IDENTIFIED : I D E N T I F I E D;
K_INSERT : I N S E R T;
K_INTO : I N T O;
K_JOIN : J O I N;
K_KEY : K E Y;
K_NOT : N O T;
K_NULL : N U L L;
K_ON : O N;
K_PRIMARY : P R I M A R Y;
K_QUIT : Q U I T;
K_REVOKE : R E V O K E;
K_SELECT : S E L E C T;
K_SET : S E T;
K_SHOW : S H O W;
K_TABLE : T A B L E;
K_TO : T O;
K_UPDATE : U P D A T E;
K_USE : U S E;
K_USER : U S E R;
K_VALUES : V A L U E S;
K_VIEW : V I E W;
K_WHERE : W H E R E;

IDENTIFIER :
    [a-zA-Z_] [a-zA-Z_0-9]* ;

NUMERIC_LITERAL :
    DIGIT+ EXPONENT?
    | DIGIT+ '.' DIGIT* EXPONENT?
    | '.' DIGIT+ EXPONENT? ;

EXPONENT :
    E [-+]? DIGIT+ ;

STRING_LITERAL :
    '\'' ( ~'\'' | '\'\'' )* '\'' ;

SINGLE_LINE_COMMENT :
    '--' ~[\r\n]* -> channel(HIDDEN) ;

MULTILINE_COMMENT :
    '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN) ;

SPACES :
    [ \u000B\t\r\n] -> channel(HIDDEN) ;

fragment DIGIT : [0-9] ;
fragment A : [aA] ;
fragment B : [bB] ;
fragment C : [cC] ;
fragment D : [dD] ;
fragment E : [eE] ;
fragment F : [fF] ;
fragment G : [gG] ;
fragment H : [hH] ;
fragment I : [iI] ;
fragment J : [jJ] ;
fragment K : [kK] ;
fragment L : [lL] ;
fragment M : [mM] ;
fragment N : [nN] ;
fragment O : [oO] ;
fragment P : [pP] ;
fragment Q : [qQ] ;
fragment R : [rR] ;
fragment S : [sS] ;
fragment T : [tT] ;
fragment U : [uU] ;
fragment V : [vV] ;
fragment W : [wW] ;
fragment X : [xX] ;
fragment Y : [yY] ;
fragment Z : [zZ] ;
