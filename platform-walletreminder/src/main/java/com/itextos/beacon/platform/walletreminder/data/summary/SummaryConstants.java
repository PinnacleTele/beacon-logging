package com.itextos.beacon.platform.walletreminder.data.summary;

class SummaryConstants
{

    protected static final String   HTML_TOP       = "<html><head><style>table{font-family:arial,sans-serif;border-collapse:collapse;width:100%;}th{border:1px solid #808080;background-color:#dddddd;padding:8px;}td{border:1px solid #808080;padding:8px;}</style></head><link rel=\"stylesheet\" href=\"css/common.css\"><body><table border ='0' align='center'><tr ><td align='center'><b><u>";
    protected static final String   HTML_HEADER    = "</u><b></td></tr><tr ><td>&nbsp;</td></tr><tr><td><table border ='1' align='center'><tr>";
    protected static final String   HTML_BOTTOM    = "</table></td></tr></table></body></html>";
    protected static final String   TD_ALIGN_LEFT  = "<td align='left'>";
    protected static final String   TD_ALIGN_RIGHT = "<td align='right'>";
    protected static final String   TD_CLOSE       = "</td>";
    protected static final String   TR_OPEN        = "<tr>";

    protected static final String[] CLIID_HEADERS  =
    { "S. No", "Client Id", "User", "Wallet Balance", "Client Name", "Client Email" };
    protected static final String[] USER_HEADERS   =
    { "S. No", "User", "Client Id", "Wallet Balance", "Client Name", "Client Email" };

}
