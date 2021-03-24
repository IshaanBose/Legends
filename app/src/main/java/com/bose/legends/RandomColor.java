package com.bose.legends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomColor
{
    private final List<Integer> colors;

    public RandomColor(List<Integer> exclude)
    {
        this.colors = new ArrayList<>();

        this.colors.addAll(Arrays.asList(
                0xFF57D9C7, 0xFF11C8F3, 0xFF5D3499, 0xFF43719A, 0xFF822035,
                0xFF8161F9, 0xFFF52CC1, 0xFFB3EF7F, 0xFF4AEB9E, 0xFF3FBE13,
                0xFFB10845, 0xFFDE15AC, 0xFF66A1CF, 0xFF4D408E, 0xFF85F1E8,
                0xFF182376, 0xFF98DE57, 0xFF907E0C, 0xFFE294A9, 0xFFF8A650,
                0xFFA0BBCF, 0xFFA06891, 0xFFC64779, 0xFF0FB901, 0xFFA83B24,
                0xFF51E54F, 0xFFBC86F1, 0xFF8563E0, 0xFFA0B0D9, 0xFF5D7D6A,
                0xFFF54CAB, 0xFFC25DB6, 0xFF707B52, 0xFF6000B1, 0xFF8AB1B9,
                0xFF0A44E7, 0xFF2A4939, 0xFF652BDE, 0xFFE2915F, 0xFF1CA8D5,
                0xFF1CFB3B, 0xFFCEBD16, 0xFFDF0FE6, 0xFF1CB684, 0xFFA6C9E8,
                0xFF43FCAF, 0xFFD17DBA, 0xFFF4D858, 0xFF41681D, 0xFF33E9CB,
                0xFF6E4898, 0xFF9BBF4C, 0xFF5042D9, 0xFFC6444C, 0xFF0F77EF,
                0xFF0A8DE6, 0xFF1A3992, 0xFFF4D42B, 0xFF6E0830, 0xFFDB2361,
                0xFF8CF1E3, 0xFFB54607, 0xFF9A2997, 0xFFCAFEE7, 0xFF90599E,
                0xFFF8C262, 0xFF46B23A, 0xFF4EB886, 0xFFBF9249, 0xFF8607D2,
                0xFFA286BA, 0xFFC217B5, 0xFF27AAD9, 0xFF139A7B, 0xFF492D4D,
                0xFF2D7DEA, 0xFFEF61B2, 0xFF0F2F36, 0xFFF8B8AE, 0xFFF94079,
                0xFF0CF372, 0xFF73EADA, 0xFF8DCEFE, 0xFFA7A2D9, 0xFF633223,
                0xFFF1CC51, 0xFF0582C1, 0xFFC138B3, 0xFF19E241, 0xFFB02B6B,
                0xFF1FE7F9, 0xFFAF91BA, 0xFF99105E, 0xFF52815B, 0xFFEA53A5,
                0xFF1A686E, 0xFFF65688, 0xFF91563B, 0xFF03862B, 0xFFE94848 // 5 * 20 = 100
        ));

        if (exclude != null && exclude.size() != 0)
            this.colors.removeAll(exclude);
    }

    public String getRandomColor()
    {
        Random random = new Random();
        int randomColor = this.colors.get(random.nextInt(this.colors.size()));

        return "#" + Integer.toHexString(randomColor);
    }
}
