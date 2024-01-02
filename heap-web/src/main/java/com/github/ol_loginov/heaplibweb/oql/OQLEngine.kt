package com.github.ol_loginov.heaplibweb.oql

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Stream

private val log = LoggerFactory.getLogger(OQLEngine::class.java)

open class OQLEngine {
    private fun execute(statement: String) {
        val lexer = OQLLexer(CharStreams.fromString(statement))
        lexer.addErrorListener(ANTLRErrorListenerAdapter())

        val parser = OQLParser(BufferedTokenStream(lexer))
        parser.addErrorListener(ANTLRErrorListenerAdapter())

        val statementImpl = parser.selectStatement()
    }

    fun executeForStream(statement: String): Stream<Any?> {
        execute(statement)
        return Stream.empty()
    }

    class ANTLRErrorListenerAdapter : ANTLRErrorListener {
        override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
            log.error("!!")
        }

        override fun reportAmbiguity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, exact: Boolean, ambigAlts: BitSet?, configs: ATNConfigSet?) {
            log.info("ambiguity")
        }

        override fun reportAttemptingFullContext(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, conflictingAlts: BitSet?, configs: ATNConfigSet?) {
            log.info("attempting full context")
        }

        override fun reportContextSensitivity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, prediction: Int, configs: ATNConfigSet?) {
            log.info("report context sensitivity")
        }
    }
}