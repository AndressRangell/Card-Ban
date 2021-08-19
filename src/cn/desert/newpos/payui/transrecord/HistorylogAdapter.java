package cn.desert.newpos.payui.transrecord;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.utils.ISOUtil;
import com.wposs.cobranzas.R;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;

import java.util.Locale;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.MasterControl;

import static cn.desert.newpos.payui.UIUtils.labelHTML;
import static com.newpos.libpay.trans.Trans.idLote;

public class HistorylogAdapter extends ListAdapter<TransLogData> {

    static String clase = "HistorylogAdapter.java";
    TMConfig config;

    public HistorylogAdapter(Activity context) {
        super(context);
        config = TMConfig.getInstance();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHold viewHold = null;
        TransLogData item = null;
        if (!mList.isEmpty()) {
            item = mList.get(position);
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_history_item, null);
            viewHold = new ViewHold();
            viewHold.tvPan = (TextView) convertView.findViewById(R.id.tv_pan);
            viewHold.tvVoucherno = (TextView) convertView.findViewById(R.id.tv_voucherno);
            viewHold.tvAuthno = (TextView) convertView.findViewById(R.id.tv_authno);
            viewHold.tvAmount = (TextView) convertView.findViewById(R.id.tv_amount);
            viewHold.tvTip = (TextView) convertView.findViewById(R.id.tv_tip);
            viewHold.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            viewHold.tvBatchno = (TextView) convertView.findViewById(R.id.tv_batchno);
            viewHold.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
            viewHold.tvRight_top = (TextView) convertView.findViewById(R.id.status_flag);
            viewHold.reprint = (Button) convertView.findViewById(R.id.re_print);
            convertView.setTag(viewHold);
        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        if (item != null) {
            String pan = item.getPan();
            if (!PAYUtils.isNullWithTrim(pan)) {
                String temp;
                if (item.isScan()) {
                    temp = labelHTML(UIUtils.getStringByInt(mContext, R.string.pay_code), ISOUtil.ofuscarPAN(pan));
                } else {
                    temp = labelHTML(UIUtils.getStringByInt(mContext, R.string.card_num), ISOUtil.ofuscarPAN(pan));
                }
                viewHold.tvPan.setText(Html.fromHtml(temp));
            }

            String auth = item.getRrn();
            if (!PAYUtils.isNullWithTrim(auth)) {
                viewHold.tvAuthno.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.auth_code), auth)));
            }

            final String nroCargo = item.getNroCargo();
            if (!PAYUtils.isNullWithTrim(nroCargo)) {
                viewHold.tvVoucherno.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.voucher_num), nroCargo)));
            }

            String amount = "0";
            if (item.getAmount() != null) {
                amount = item.getAmount().toString();
            }
            if (!PAYUtils.isNullWithTrim(amount)) {
                viewHold.tvAmount.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.amount), " Gs. " + PAYUtils.FormatPyg(amount))));
            }
            viewHold.tvTip.setVisibility(View.GONE);

            try {
                TransLog transLog = TransLog.getInstance(idLote);
                if (transLog != null) {
                    TransLogData transLogData = transLog.searchTransLogByNroCargo(nroCargo);
                    if (transLogData != null) {

                        viewHold.tvStatus.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.trans_type), transLogData.getTipoVenta())));
                    }
                } else {
                    String en = item.getEName();
                    if (!PAYUtils.isNullWithTrim(en)) {
                        if (Locale.getDefault().getLanguage().equals("zh")) {
                            viewHold.tvStatus.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.trans_type), MasterControl.en2ch(en))));
                        } else {
                            viewHold.tvStatus.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.trans_type), en.replace("_", " "))));
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                e.printStackTrace();
            }


            if (item.getIsVoided()) {
                viewHold.tvRight_top.setVisibility(View.VISIBLE);
                viewHold.tvRight_top.setText(UIUtils.getStringByInt(mContext, R.string.is_revocation));
            } else {
                viewHold.tvRight_top.setVisibility(View.GONE);
            }

            viewHold.tvDate.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.trans_date), PAYUtils.printStr(item.getLocalDate(), item.getLocalTime()))));

            String bacth = item.getBatchNo();
            if (!PAYUtils.isNullWithTrim(bacth)) {
                viewHold.tvBatchno.setVisibility(View.GONE);
                viewHold.tvBatchno.setText(Html.fromHtml(labelHTML(UIUtils.getStringByInt(mContext, R.string.batch_num), bacth)));
            }


            convertView.setTag(R.id.tag_item_history_trans, item);
        }
        return convertView;
    }

    final class ViewHold {
        TextView tvPan;
        TextView tvVoucherno;
        TextView tvAuthno;
        TextView tvAmount;
        TextView tvTip;
        TextView tvDate;
        TextView tvBatchno;
        TextView tvStatus;
        TextView tvRight_top;
        Button reprint;
    }
}
