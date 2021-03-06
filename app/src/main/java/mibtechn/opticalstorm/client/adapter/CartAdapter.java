package mibtechn.opticalstorm.client.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import mibtechn.opticalstorm.client.R;
import mibtechn.opticalstorm.client.fragment.CartFragment;
import mibtechn.opticalstorm.client.helper.ApiConfig;
import mibtechn.opticalstorm.client.helper.Constant;
import mibtechn.opticalstorm.client.helper.Session;
import mibtechn.opticalstorm.client.model.Cart;


public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    public boolean isLoading;
    Activity activity;
    ArrayList<Cart> items;
    String taxPercentage;
    Context context;
    Session session;


    public CartAdapter(Context context, Activity activity, ArrayList<Cart> items) {
        this.context = context;
        this.activity = activity;
        this.items = items;
        session = new Session(context);
        taxPercentage = "0";
    }

    public void add(int position, Cart item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false);
            return new ProductHolderItems(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
            return new ViewHolderLoading(view);
        }

        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderparent, final int position) {
        try {

            if (holderparent instanceof ProductHolderItems) {
                final ProductHolderItems holder = (ProductHolderItems) holderparent;
                final Cart cart = items.get(position);

                double price = 0;

                try {
                    taxPercentage = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
                } catch (Exception e) {

                }

                Picasso.get()
                        .load(cart.getItems().get(0).getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgproduct);

                holder.txtproductname.setText(cart.getItems().get(0).getName());

                holder.txtmeasurement.setText(cart.getItems().get(0).getMeasurement() + "\u0020" + cart.getItems().get(0).getUnit());

                holder.txtprice.setText(session.getData(Constant.currency) + Constant.formater.format(Double.parseDouble(cart.getItems().get(0).getDiscounted_price())));

                if (cart.getItems().get(0).getIsAvailable().equals("false")) {
                    holder.txtstatus.setVisibility(View.VISIBLE);
                    holder.txtstatus.setText(activity.getString(R.string.sold_out));
                    holder.lytqty.setVisibility(View.GONE);
                    CartFragment.isSoldOut = true;
                }

                if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                    price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                    holder.txtprice.setText(session.getData(Constant.currency) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100))));
                } else {
                    price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    holder.txtoriginalprice.setPaintFlags(holder.txtoriginalprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.txtoriginalprice.setText(session.getData(Constant.currency) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100))));

                    holder.txtprice.setText(session.getData(Constant.currency) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100))));
                }

                holder.txtQuantity.setText(cart.getQty());

                holder.txttotalprice.setText(session.getData(Constant.currency) + Constant.formater.format(price * Integer.parseInt(cart.getQty())));

                final double finalPrice = price;
                holder.btnaddqty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ApiConfig.isConnected(activity)) {
                            if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) >= Float.parseFloat(cart.getItems().get(0).getStock()))) {
                                if (!(Integer.parseInt(holder.txtQuantity.getText().toString()) + 1 > Integer.parseInt(session.getData(Constant.max_cart_items_count)))) {
                                    int count = Integer.parseInt(holder.txtQuantity.getText().toString());
                                    count++;
                                    holder.txtQuantity.setText("" + count);
                                    holder.txttotalprice.setText(session.getData(Constant.currency) + Constant.formater.format(finalPrice * count));
                                    Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT + finalPrice;
                                    if (CartFragment.values.containsKey(items.get(position).getProduct_variant_id())) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            CartFragment.values.replace(items.get(position).getProduct_variant_id(), "" + count);
                                        } else {
                                            CartFragment.values.remove(items.get(position).getProduct_variant_id());
                                            CartFragment.values.put(items.get(position).getProduct_variant_id(), "" + count);
                                        }
                                    } else {
                                        CartFragment.values.put(items.get(position).getProduct_variant_id(), "" + count);
                                    }
                                    CartFragment.SetData();
                                } else {
                                    Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                holder.btnminusqty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ApiConfig.isConnected(activity)) {
                            if (Integer.parseInt(holder.txtQuantity.getText().toString()) > 1) {
                                int count = Integer.parseInt(holder.txtQuantity.getText().toString());
                                count--;
                                holder.txtQuantity.setText("" + count);
                                holder.txttotalprice.setText(session.getData(Constant.currency) + Constant.formater.format(finalPrice * count));
                                Constant.FLOAT_TOTAL_AMOUNT = Constant.FLOAT_TOTAL_AMOUNT - finalPrice;
                                if (CartFragment.values.containsKey(items.get(position).getProduct_variant_id())) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        CartFragment.values.replace(items.get(position).getProduct_variant_id(), "" + count);
                                    } else {
                                        CartFragment.values.remove(items.get(position).getProduct_variant_id());
                                        CartFragment.values.put(items.get(position).getProduct_variant_id(), "" + count);
                                    }
                                } else {
                                    CartFragment.values.put(items.get(position).getProduct_variant_id(), "" + count);
                                }
                                CartFragment.SetData();
                            }
                        }
                    }
                });

                holder.imgdelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ApiConfig.isConnected(activity)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(activity.getResources().getString(R.string.deleteproducttitle));
                            builder.setIcon(android.R.drawable.ic_delete);
                            builder.setMessage(activity.getResources().getString(R.string.deleteproductmsg));

                            builder.setCancelable(false);
                            builder.setPositiveButton(activity.getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    Constant.FLOAT_TOTAL_AMOUNT -= (finalPrice * Integer.parseInt(holder.txtQuantity.getText().toString()));

                                    if (CartFragment.values.containsKey(items.get(position).getProduct_variant_id())) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            CartFragment.values.replace(items.get(position).getProduct_variant_id(), "0");
                                        } else {
                                            CartFragment.values.remove(items.get(position).getProduct_variant_id());
                                            CartFragment.values.put(items.get(position).getProduct_variant_id(), "0");
                                        }
                                    } else {
                                        CartFragment.values.put(items.get(position).getProduct_variant_id(), "0");
                                    }
                                    CartFragment.SetData();

                                    items.remove(cart);
                                    CartFragment.isSoldOut = false;
                                    notifyDataSetChanged();
                                    Constant.TOTAL_CART_ITEM = getItemCount();
                                    CartFragment.SetData();
                                    activity.invalidateOptionsMenu();
                                    if (getItemCount() == 0) {
                                        CartFragment.lytempty.setVisibility(View.VISIBLE);
                                        CartFragment.lytTotal.setVisibility(View.GONE);
                                    }
                                }
                            });

                            builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }

                    }
                });

            } else if (holderparent instanceof ViewHolderLoading) {
                ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderparent;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Cart cart = items.get(position);
        if (cart != null)
            return Integer.parseInt(cart.getId());
        else
            return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class ProductHolderItems extends RecyclerView.ViewHolder {
        ImageView imgproduct, imgdelete, btnminusqty, btnaddqty;
        TextView txtproductname, txtmeasurement, txtprice, txtoriginalprice, txtQuantity, txttotalprice, txtstatus;
        LinearLayout lytqty;

        public ProductHolderItems(@NonNull View itemView) {
            super(itemView);
            imgproduct = itemView.findViewById(R.id.imgproduct);

            imgdelete = itemView.findViewById(R.id.imgdelete);
            btnminusqty = itemView.findViewById(R.id.btnminusqty);
            btnaddqty = itemView.findViewById(R.id.btnaddqty);

            txtproductname = itemView.findViewById(R.id.txtproductname);
            txtmeasurement = itemView.findViewById(R.id.txtmeasurement);
            txtprice = itemView.findViewById(R.id.txtprice);
            txtoriginalprice = itemView.findViewById(R.id.txtoriginalprice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txttotalprice = itemView.findViewById(R.id.txttotalprice);
            txtstatus = itemView.findViewById(R.id.txtstatus);

            lytqty = itemView.findViewById(R.id.lytqty);
        }
    }
}