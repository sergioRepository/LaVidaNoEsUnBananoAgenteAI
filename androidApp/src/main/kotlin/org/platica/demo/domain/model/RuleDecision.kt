package org.platica.demo.domain.model

sealed interface RuleDecision {
    data class Intervene(val reason: InterventionReason) : RuleDecision
    data class NoAction(val blockReason: BlockReason) : RuleDecision
}
