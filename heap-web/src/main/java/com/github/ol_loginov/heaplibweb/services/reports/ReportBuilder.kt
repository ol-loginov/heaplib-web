package com.github.ol_loginov.heaplibweb.services.reports

import org.springframework.transaction.support.TransactionOperations

interface ReportBuilder : (TransactionOperations) -> Unit {
}
