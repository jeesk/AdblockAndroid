package io.github.edsuns.adfilter

import io.github.edsuns.adblockclient.Client
import io.github.edsuns.adblockclient.MatchResult
import io.github.edsuns.adblockclient.ResourceType
import io.github.edsuns.adblockclient.isException
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Edsuns@qq.com on 2020/10/24.
 */
interface AbstractDetector {
    fun addClient(client: Client)
    fun removeClient(id: String)
    fun clearAllClient()
    fun shouldBlock(url: String, documentUrl: String, resourceType: ResourceType): String?
    fun getElementHidingSelectors(documentUrl: String): String
    fun getCustomElementHidingSelectors(documentUrl: String): String
    fun getExtendedCssSelectors(documentUrl: String): List<String>
    fun getCssRules(documentUrl: String): List<String>
    fun getScriptlets(documentUrl: String): List<String>
}

internal class Detector : AbstractDetector {

    val clients = CopyOnWriteArrayList<Client>()

    // null means disabled
    internal var customFilterClient: Client? = null
        set(value) {
            field = value
            Timber.v("Blacklist client changed")
        }

    override fun addClient(client: Client) {
        clients.removeAll { it.id == client.id }
        clients.add(client)
        Timber.v("Client count: ${clients.size} (after addClient)")
    }

    override fun removeClient(id: String) {
        clients.removeAll { it.id == id }
        Timber.v("Client count: ${clients.size} (after removeClient)")
    }

    override fun clearAllClient() {
        clients.clear()
        Timber.v("Client count: ${clients.size} (after clearAllClient)")
    }

    /**
     * returns not null if should block the web resource
     */
    override fun shouldBlock(
        url: String,
        documentUrl: String,
        resourceType: ResourceType
    ): String? {
        customFilterClient?.matches(url, documentUrl, resourceType)?.let {
            if (it.isException) {
                return null// don't block exception
            }
            if (it.shouldBlock) {
                return it.matchedRule ?: ""
            }
        }
        var matchResult: MatchResult? = null
        for (client in clients) {
            val match: MatchResult = client.matches(url, documentUrl, resourceType)
            if (match.isException) {
                return null// don't block exception
            }
            if (match.shouldBlock) {
                matchResult = match
            }
        }
        return if (matchResult?.shouldBlock == true) matchResult.matchedRule ?: "" else null
    }

    override fun getElementHidingSelectors(documentUrl: String): String {
        val builder = StringBuilder()
        for (client in clients) {
            val selector = client.getElementHidingSelectors(documentUrl)
            if (!selector.isNullOrBlank()) {
                if (builder.isNotEmpty()) {
                    builder.append(", ")
                }
                builder.append(selector)
            }
        }
        return builder.toString()
    }

    override fun getCustomElementHidingSelectors(documentUrl: String): String {
        return customFilterClient?.getElementHidingSelectors(documentUrl) ?: ""
    }

    private fun getRulesIntoList(transform: (client: Client) -> Array<String>?): List<String> {
        val result = ArrayList<String>()
        for (client in clients) {
            val rules = transform(client) ?: continue
            result.addAll(rules)
        }
        // TODO: validate custom rules because rules containing any wrong will break
        customFilterClient?.let {
            val rules = transform(it) ?: return@let
            result.addAll(rules)
        }
        return result
    }

    override fun getExtendedCssSelectors(documentUrl: String): List<String> =
        getRulesIntoList { it.getExtendedCssSelectors(documentUrl) }

    override fun getCssRules(documentUrl: String): List<String> =
        getRulesIntoList { it.getCssRules(documentUrl) }

    override fun getScriptlets(documentUrl: String): List<String> =
        getRulesIntoList { it.getScriptlets(documentUrl) }

}