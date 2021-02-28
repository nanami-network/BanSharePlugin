package xyz.n7mn.dev.banshareplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.n7mn.dev.banshareplugin.data.BanData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BanCommand implements CommandExecutor {

    private final Connection con;
    public BanCommand(Connection con){
        this.con = con;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player exePlayer = null;

        if (sender instanceof Player){
            exePlayer = (Player) sender;
        }

        if (exePlayer != null && (!exePlayer.isOp() && !exePlayer.hasPermission("7misys.ban"))){
            exePlayer.sendMessage(ChatColor.RED + "権限がありません。");
            return true;
        }

        if (label.toLowerCase().startsWith("gban")){

            if (args.length >= 2){

                Player player = Bukkit.getServer().getPlayer(args[0]);
                if (player == null){
                    sender.sendMessage(ChatColor.YELLOW + "いまログインしていないユーザーです！！");
                    return true;
                }

                List<BanData> banDataList = new ArrayList<>();
                try {
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM BanList");
                    ResultSet set = statement.executeQuery();
                    while(set.next()){
                        banDataList.add(
                                new BanData(
                                        set.getInt("BanID"),
                                        UUID.fromString(set.getString("UserUUID")),
                                        set.getString("Reason"),
                                        set.getString("Area"),
                                        set.getString("IP"),
                                        set.getDate("EndDate"),
                                        set.getDate("ExecuteDate"),
                                        UUID.fromString(set.getString("ExecuteUserUUID")),
                                        set.getBoolean("Active")
                                )
                        );
                    }

                    PreparedStatement statement2 = con.prepareStatement("INSERT INTO `BanList` (`BanID`, `UserUUID`, `Reason`, `Area`, `IP`, `EndDate`, `ExecuteDate`, `ExecuteUserUUID`, `Active`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
                    statement2.setInt(1, banDataList.size() + 1);
                    statement2.setString(2, player.getUniqueId().toString());
                    statement2.setString(3, args[1]);
                    statement2.setString(4, Bukkit.getServer().getPluginManager().getPlugin("BanSharePlugin").getConfig().getString("Area"));
                    statement2.setString(5, player.getAddress().getHostName());
                    statement2.setString(6, "9999-12-31 23:59:59");
                    statement2.setDate(7, new Date(new java.util.Date().getTime()));
                    if (exePlayer != null){
                        statement2.setString(8, exePlayer.getUniqueId().toString());
                    } else {
                        statement2.setString(8, "console");
                    }
                    statement2.setBoolean(9, true);

                    statement2.execute();
                    statement2.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }


                Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
                for (Player player1 : onlinePlayers){

                    if (player1.isOp() || player1.hasPermission("7misys.ban")){
                        player1.sendMessage(ChatColor.GREEN + player.getName() + "を" + "「"+args[1]+"」という理由でBANしました。");
                    }

                }
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "/gban <プレーヤー名> <理由>");
        }


        return true;
    }
}
