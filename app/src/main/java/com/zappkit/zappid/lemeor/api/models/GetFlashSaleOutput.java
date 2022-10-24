package com.zappkit.zappid.lemeor.api.models;

import com.google.gson.annotations.SerializedName;
import com.zappkit.zappid.lemeor.models.Advertisements;
import com.zappkit.zappid.lemeor.models.FlashSale;
import com.zappkit.zappid.lemeor.models.Reminder;

public class GetFlashSaleOutput {
    @SerializedName("flash_sale")
    public FlashSale flashSale;
    @SerializedName("reminder")
    public Reminder reminder;
    @SerializedName("advertisements")
    public Advertisements advertisements;
}
