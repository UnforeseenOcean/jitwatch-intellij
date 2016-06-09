package ru.yole.jitwatch

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.NavigatablePsiElement
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class JitReportToolWindow(val project: Project) : JPanel(BorderLayout()) {
    private val reportTable = TableView<InlineFailureInfo>()
    private val reportTableModel = ListTableModel<InlineFailureInfo>(CallSiteColumnInfo, CalleeColumnInfo, ReasonColumnInfo)

    init {
        reportTable.setModelAndUpdateColumns(reportTableModel)
        add(reportTable, BorderLayout.CENTER)

        reportTableModel.items = JitWatchModelService.getInstance(project).inlineFailures

        reportTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1 && e.clickCount == 2) {
                    navigateToSelectedCall()
                }
            }
        })
    }

    private fun navigateToSelectedCall() {
        val failureInfo = reportTable.selectedObject ?: return
        val psiMethod = JitWatchModelService.getInstance(project).getPsiMember(failureInfo.callSite) ?: return
        (psiMethod as? NavigatablePsiElement)?.navigate(true)
    }
}

object CallSiteColumnInfo : ColumnInfo<InlineFailureInfo, String>("Call site") {
    override fun valueOf(item: InlineFailureInfo): String? {
        return item.callSite.fullyQualifiedMemberName
    }
}

object CalleeColumnInfo : ColumnInfo<InlineFailureInfo, String>("Callee") {
    override fun valueOf(item: InlineFailureInfo): String? {
        return item.callee.fullyQualifiedMemberName
    }
}

object ReasonColumnInfo : ColumnInfo<InlineFailureInfo, String>("Reason") {
    override fun valueOf(item: InlineFailureInfo): String? {
        return item.reason
    }
}

class JitReportToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(
                ContentFactory.SERVICE.getInstance().createContent(JitReportToolWindow(project), "", false)
        )
    }
}

