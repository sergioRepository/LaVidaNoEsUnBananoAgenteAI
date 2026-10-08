package com.lavidanoesunbanano.domain.model

sealed interface RuleDecision {
    data class Intervene(val reason: InterventionReason) : RuleDecision
    data class NoAction(val blockReason: BlockReason) : RuleDecision
}
