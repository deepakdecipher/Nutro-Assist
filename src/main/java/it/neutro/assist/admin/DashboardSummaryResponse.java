package it.neutro.assist.admin;

public record DashboardSummaryResponse(
        long totalUsers,
        long totalAdmins,
        long totalRegularUsers,
        long totalActiveUsers,
        Long totalPlans,
        Long revenue
) {
}
