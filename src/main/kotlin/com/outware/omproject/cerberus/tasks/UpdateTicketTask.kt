package com.outware.omproject.cerberus.tasks

import com.outware.omproject.cerberus.CerberusPlugin
import com.outware.omproject.cerberus.data.JiraClient
import com.outware.omproject.cerberus.data.model.JiraCommentRequest
import com.outware.omproject.cerberus.exceptions.GenericHttpException
import com.outware.omproject.cerberus.exceptions.HttpAuthenticationException
import com.outware.omproject.cerberus.util.buildComment
import com.outware.omproject.cerberus.util.getBuildTickets

open class UpdateTicketTask : NonEssentialTask() {

    override fun run() {
        val tickets = getBuildTickets()

        val ticketComment = buildComment(CerberusPlugin.properties?.buildNumber,
                CerberusPlugin.properties?.buildUrl,
                CerberusPlugin.properties?.hockeyAppShortVersion,
                CerberusPlugin.properties?.hockeyAppUploadUrl)

        tickets.forEach {
            commentOnJiraTicket(it.key, ticketComment)
        }
    }

    private fun commentOnJiraTicket(ticket: String, message: String) {
        val client = JiraClient("/rest/api/2/issue/$ticket/comment")

        val responseCode = client.post(JiraCommentRequest(message))

        when (responseCode) {
            201 -> println("Successfully commented on $ticket")
            in (400..499) -> {
                throw HttpAuthenticationException("Authentication failed. HTTP response code: ${client.responseCode}")
            }
            else -> {
                throw GenericHttpException("Comment on $ticket failed. HTTP response code: ${client.responseCode}")
            }
        }
    }
}
