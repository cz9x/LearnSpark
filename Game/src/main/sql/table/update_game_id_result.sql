DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_game_id_result`()
begin
update game_result a, game_info b
set a.game_id = b.id
where a.home_team = b.home_team
  and a.guest_team = b.guest_team
  and a.game_time = b.game_time;
end;;
DELIMITER ;