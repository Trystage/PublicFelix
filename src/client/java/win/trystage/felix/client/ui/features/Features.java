package win.trystage.felix.client.ui.features;

import net.minecraft.client.MinecraftClient;
import win.trystage.felix.client.core.CommandExecutor;

import java.util.List;

public final class Features {
    private Features() {}
    public static MenuFeature teleport() { return new TeleportPlayerFeature(); }
    public static MenuFeature ban() { return new BanPlayerFeature(); }
    public static MenuFeature mute() { return new MutePlayerFeature(); }
    public static MenuFeature history() { return new HistoryQueryFeature(); }
    public static MenuFeature report() { return new ReportPlayerFeature(); }
    public static MenuFeature qna() { return new QnaFeature(); }
    public static MenuFeature warn() { return new WarnFeature(); }
}
class TeleportPlayerFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("传送到此服务器", "传送到此", "传送到所在服务器", "传送到玩家", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        switch (option) {
            case "传送到此服务器":
                if (client.player != null) {
                    CommandExecutor.sendServerCommand("btp " + targetPlayerName + " " + client.player.getName().getString());
                }
                break;
            case "传送到此":
                if (client.player != null) {
                    CommandExecutor.sendServerCommand("tp " + targetPlayerName + " " + client.player.getName().getString());
                }
                break;
            case "传送到所在服务器":
                CommandExecutor.sendServerCommand("btp " + targetPlayerName);
                break;
            case "传送到玩家":
                CommandExecutor.sendServerCommand("tp " + targetPlayerName);
                break;
            default:
                break;
        }
    }
}
class BanPlayerFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("开挂作弊", "联合组队", "欺负队友", "利用漏洞", "可疑账号", "垃圾广告", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String reason = switch (option) {
            case "开挂作弊" -> "Cheating";
            case "联合组队" -> "CrossTeaming";
            case "欺负队友" -> "InterfereTeammates";
            case "利用漏洞" -> "ExploitingBug";
            case "可疑账号" -> "AccountSuspicious";
            case "垃圾广告" -> "ADS";
            default -> "";
        };
        if (!reason.isEmpty()) {
            CommandExecutor.openChatWithCommand("ban " + targetPlayerName + " " + reason, client);
        }
    }
}
class MutePlayerFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("刷屏", "歧视行为", "戏弄玩家", "不合适的言论", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String reason = switch (option) {
            case "刷屏" -> "Spam";
            case "歧视行为" -> "Discrimination";
            case "戏弄玩家" -> "Troll";
            case "不合适的言论" -> "Inappropriate";
            default -> "";
        };
        if (!reason.isEmpty()) {
            CommandExecutor.openChatWithCommand("mute " + targetPlayerName + " " + reason, client);
        }
    }
}
class HistoryQueryFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("最近10条", "最近15条", "所有记录", "返回");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String duration = switch (option) {
            case "最近10条" -> "10";
            case "最近15条" -> "15";
            default -> "100";
        };
        CommandExecutor.sendServerCommand("history " + targetPlayerName + " " + duration);
    }
}
class ReportPlayerFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("作弊行为", "不良言论", "其它", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String reason = switch (option) {
            case "作弊行为" -> "作弊行为";
            case "不良言论" -> "不良言论";
            case "其它" -> "其它";
            default -> "";
        };
        if (!reason.isEmpty()) {
            if (reason.equals("其它")) {
                CommandExecutor.sendServerCommand("report " + targetPlayerName + " ");
            } else {
                CommandExecutor.sendServerCommand("report " + targetPlayerName + " " + reason);
            }
        }
    }
}
class QnaFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("如何获得主播类rank", "如何加入交流群", "如何反馈问题", "如何举报", "如何加入我们", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String msg = switch (option) {
            case "如何获得主播类rank" -> "在官方交流群内@管理进行申请";
            case "如何加入交流群" -> "qq交流群";
            case "如何反馈问题" -> "在官方交流群内@管理, 说明您遇到的问题, 在哪个玩法内遇到的, 您的客户端版本, 是否可复现, 复现步骤, 预期结果, 实际结果, 您的游戏ID";
            case "如何举报" -> "使用/report 玩家名 进行举报, Staff巡查会处理";
            case "如何加入我们" -> "在官方交流群内@管理, 说明您要申请的岗位, 我们稍后会把您拉进一个临时群聊中进行评估审核";
            default -> "";
        };
        if (!msg.isEmpty()) {
            CommandExecutor.sendChatMessage(msg);
        }
    }
}
class WarnFeature implements MenuFeature {
    @Override
    public List<String> options() {
        return List.of("开挂作弊", "消息违规", "刷屏广告", "取消");
    }
    @Override
    public void execute(int optionIndex, String targetPlayerName, MinecraftClient client) {
        String option = options().get(optionIndex);
        String reason = switch (option) {
            case "开挂作弊" -> "疑似作弊";
            case "消息违规" -> "消息违规";
            case "刷屏广告" -> "消息内容刷屏或在公共聊天发送广告";
            default -> "";
        };
        if (!reason.isEmpty()) {
            CommandExecutor.sendServerCommand("warn " + targetPlayerName + " " + reason);
        }
    }
}