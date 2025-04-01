package com.afkanerd.deku.RemoteListeners.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class RemoteListenersQueues {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public long gatewayClientId;

    public String name;
    public String binding1Name;
    public String binding2Name;


    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof RemoteListenersQueues) {
            RemoteListenersQueues remoteListenersQueues = (RemoteListenersQueues) obj;

            return remoteListenersQueues.id == this.id &&
                    Objects.equals(remoteListenersQueues.name, this.name) &&
                    Objects.equals(remoteListenersQueues.binding1Name, this.binding1Name) &&
                    Objects.equals(remoteListenersQueues.binding2Name, this.binding2Name) &&
                    remoteListenersQueues.gatewayClientId == this.gatewayClientId;
        }
        return false;
    }

    public static final DiffUtil.ItemCallback<RemoteListenersQueues> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RemoteListenersQueues>() {
                @Override
                public boolean areItemsTheSame(@NonNull RemoteListenersQueues oldItem,
                                               @NonNull RemoteListenersQueues newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull RemoteListenersQueues oldItem,
                                                  @NonNull RemoteListenersQueues newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
