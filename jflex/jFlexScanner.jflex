package org.upl;

import org.upl.Main;
import static org.upl.lexer.TokenType.*;

%%
%type Token
%line
%column
%char
%class JFlexScanner
%{
%}
%eofval{
	return new Token(EOF, "", null, yyline + 1, yycolumn);
%eofval}

InputCharacter = [^\r\n]

Letter =  [A-Za-z]
Digit = [0-9]
Id = {Letter}+{Digit}*
Number = {Digit}+
Type = int|bool

LineEnding = \r|\n|\r\n
WhiteSpace = {LineEnding} | [ \t\f]

TraditionalComment = "/*"~"*/"
EndOfLineComment = "//" {InputCharacter}*
Comment = {TraditionalComment} | {EndOfLineComment}

%%
"begin" { return new Token(BEGIN, yytext(), null, yyline + 1, yycolumn); }
"end" { return new Token(END, yytext(), null, yyline + 1, yycolumn); }

"if" { return new Token(IF, yytext(), null, yyline + 1, yycolumn); }
"then" { return new Token(THEN, yytext(), null, yyline + 1, yycolumn); }
"else" { return new Token(ELSE, yytext(), null, yyline + 1, yycolumn); }
"print" { return new Token(PRINT, yytext(), null, yyline + 1, yycolumn); }
"do" { return new Token(DO, yytext(), null, yyline + 1, yycolumn); }
"while" { return new Token(WHILE, yytext(), null, yyline + 1, yycolumn); }
"true" { return new Token(TRUE, yytext(), true, yyline + 1, yycolumn); }
"false" { return new Token(FALSE, yytext(), false, yyline + 1, yycolumn); }

">" { return new Token(GREATER, yytext(), null, yyline + 1, yycolumn);  }
">=" { return new Token(GREATER_EQUAL, yytext(), null, yyline + 1, yycolumn);  }
"==" { return new Token(EQUAL_EQUAL, yytext(), null, yyline + 1, yycolumn);  }
"=" { return new Token(EQUAL, yytext(), null, yyline + 1, yycolumn);  }
"(" { return new Token(LEFT_PAREN, yytext(), null, yyline + 1, yycolumn);  }
")" { return new Token(RIGHT_PAREN, yytext(), null, yyline + 1, yycolumn);  }
"{" { return new Token(LEFT_BRACE, yytext(), null, yyline + 1, yycolumn);  }
"}" { return new Token(RIGHT_BRACE, yytext(), null, yyline + 1, yycolumn);  }
";" { return new Token(SEMICOLON, yytext(), null, yyline + 1, yycolumn); }
"+" { return new Token(PLUS, yytext(), null, yyline + 1, yycolumn); }
"*" { return new Token(MULTIPLY, yytext(), null, yyline + 1, yycolumn); }

{Type} {
	if (yytext().equals("int")) return new Token(INT, yytext(), null, yyline + 1, yycolumn);
	else if (yytext().equals("bool")) return new Token(BOOL, yytext(), null, yyline + 1, yycolumn);
	Main.error(yyline + 1, yycolumn, "Unexpected " + yytext()); System.exit(1);
}

{Id} { return new Token(ID, yytext(), null, yyline + 1, yycolumn);}
{Number} { return new Token(NUMBER, yytext(), Integer.parseInt(yytext()), yyline + 1, yycolumn);}
{WhiteSpace} { /* ignore white space. */ return null; }
{Comment} { /* System.out.printf("Comment: %s\n", yytext()); */ return null; }

. { Main.error(yyline + 1, yycolumn,"Unexpected " + yytext()); return null; }