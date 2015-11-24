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
```java GrammarReader poetry.gram test.in```
and it would output:
```
Input: 'That wrathful cloud, gazing at itself in the light rain in the light rain, hanging.'
101011101110000000000000000
```

## Making a Grammar
There is a certain structure necessary for a grammar passed as input to one of the tools.
Firstly, each grammar consists of any number of _decisions_. These can also be called productions, if you're familiar with the normal terminology for grammars. Each grammar consists of 1 or more _paths_, each on a separate line. The syntax for a decision looks like:
```
<decision-name> {
  <path-1>
  ...
  <path-n>
}
```

When the grammar is actually used, the entry point for encoding or decoding decisions is the decision **start**.
Based on the encoded information, a path is chosen and used to generate text. Each _path_ consists of a mixture of plain text and more _decisions_. In this context, these _decisions_ are called _uses_. If you need to escape a character in the text of a _path_ (e.g. '{', '}', '[', or ']') put a slash before it. "\\\\" is read as "\" and a "\n" is read as a newline. If you want to continue a path onto the next line, a '\' followed by a newline will continue the path onto the next line. Because whitespace is ignored at the beginning of a line, if you want to start a path with a whitespace character other than newline, you should precede that character with a '\'.

Each _use_ can have three forms:
* Single-use: Indicates to make the given decision and substitute it into the path; it has the format ```[<decision-name>]```
* Saved-use: Indicates to make the given decision, substitute it into th epath, and save it in a variable; it has the format ```[<variable>=<decision-name>]```
* Re-use: Indicates to use the value stored in the given variable ```[use <variable>]```

For a small sample grammar, consider the following decisions:
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
```
String[] foo;

```

Additionally, there is another kind of decision, called an _accumulating decision_, which is denoted by preceding the body of the decision with a '+'. This represents that each path really consists of all of the previous paths followed by the listed path.

For example, here is a simple accumulating decision:
```
letters +{
  a
  b
  c
  d
}
```
It can output 4 possible encodings: 'a', 'ab', 'abc', and 'abcd'.


**For some more examples of grammars, see the _sample-grammars_ directory.**

##Limitations
There are several limitations to the grammars to make it easy to encode and decode information:
* The grammar should be un-ambiguous. If 2 combinations of decisions can produce the same encoding, then it isn't reliable for communication and decoding isn't guaranteed to work.
* Further, the grammar should be un-ambiguous with respect to the beginnings of each path. In practice, we do this by only checking the information in a path up until the first use in that path. This necessitates that no two paths within a decision have the same text before the first use, and if one path starts with a substring of another path, the path with more text must be listed first.
For example, the following decision would lead to problems:
```
sample {
  hello
  hello, world
}
```
This is because the text "hello, world" would match the first path incorrectly. Having these kinds of substrings is somewhat risky anyways, because it can easily lead to accidentally creating an ambiguous grammar. It is best to avoid these kinds of situations
* The previous point carries over to _accumulating decisions_ also, but in a different way. Any text that comes after an accumulating decision can't contain any of the strings that come before the first use in a line.
For example, the following decisions would also lead to problems:
```
acc +{
    apple
    bob
    john
}
problem {
    Here is some text: [acc]bobber
}
```
This is because the decoder would see "Here is some text: applebobber", and, upon seeing the 'bob', it would assume that the 2nd path was took, when it really wasn't. This specific case could technically be considered a limitation of the reader, however, if we replace 'bobber' with 'bob' then there is definitely an ambiguity problem. It's probably good practice to avoid these kinds of situations to begin with.
* There should be no infinite loops between decisions. Some looping is alright as long as it eventually stops.
