/**
 * Domain events for the receipts module, used to decouple committing
 * a database change from triggering the async OCR pipeline that
 * reacts to it.
 */
package bflow.receipts.event;
