# GramBGen: The Grammar-based Text Generator
This repository has a small set of tools that encodes and decodes information in a word-form by following a particular format, called a grammar.

## Encoding
Currently, we enocde information using binary strings, however this is very flexible. To encode some information use the GrammarWriter class, passing the grammar file and the binary string as arguments.
For example:
```java GrammarWriter poetry.gram 101011101110000000000000000```
produces the output
```That wrathful cloud, gazing at itself in the light rain in the light rain, hanging.```

## Decoding
To decode information, use the GrammarReader class, passing the grammar file and a file containing precisely the input you want to decode. For example if we put 
```That wrathful cloud, gazing at itself in the light rain in the light rain, hanging.```
in a file called 'test.in'.
Then to decode it you would use:
```java GrammarReade poetry.gram test.in```
and it would output:
```
Input: 'That wrathful cloud, gazing at itself in the light rain in the light rain, hanging.'
101011101110000000000000000```

## Making a Grammar
There is a certain structure necessary for a grammar passed as input to one of the tools.
Firstly, each grammar consists of any number of _decisions_. These can also be called productions, if you're familiar with the normal terminology for grammars. Each grammar consists of 1 or more _paths_, each on a separate line. The syntax for a decision looks like:
```<decision-name> {
  <path-1>
  ...
  <path-n>
}```

Based on the encoded information, a path is chosen and used to generate text. Each _path_ consists of a mixture of plain text and more _decisions_. In this context, these _decisions_ are called _uses_. If you need to escape a character in the text of a _path_ (e.g. '{', '}', '[', or ']') put a slash before it. A "\\" is read as "\" and a "\n" is read as a newline. If you want to continue a path onto the next line, a '\' followed by a newline will continue the path onto the next line. Because whitespace is ignored at the beginning of the line a path is on, if you want to start a path with a whitespace character other than newline, you should precede that character with a '\'.

Each _use_ can have three forms:
* Single-use: Indicates to make the given decision; it has the format ```[<decision-name>]```
* Saved-use: Indicates to make the given decision, and save it in a variable; it has the format ```[<variable>=<decision-name>]```
* Re-use: Indicates to use the value stored in the given variable ```[use <variable>]```

For a small example, consider the following decisions:
```
declaration {
  \ [type]\[\] [name];\n
}

type {
  int
  byte
  String
}

name {
  foo
  bar
  baz
}
```
This can produce output like:
``` String[] foo;
```

Additionally, there is another kind of decision, called an _accumulating decision_, which is denoted by preceding the body of the decision with a '+'. This represents that each path really consists of all of the previous uses followed by the use listed on the path.

For example a decision formatted like:
```
letters +{
  a
  b
  c
  d
}
```
can output 4 possible encodings: 'a', 'ab', 'abc', and 'abcd'.

**For some more examples of grammars, see the _sample-grammars_ directory.**

##Limitations
There are several limitations to the grammars to make it easy to encode and decode information:
* The grammar should be un-ambiguous. If 2 combinations of decisions can produce the same encoding, then it isn't reliable for communication and decoding isn't guaranteed to work.
* There should be no infinite loops between decisions. Some looping is alright as long as it eventually stops.