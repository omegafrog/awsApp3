import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { MessageCircle } from "lucide-react";
import { use } from "react";
import { ModeToggle } from "../ui/custom/DarkModeToggle";
import HomeMenu from "./HomeMenu";
import PostMenu from "./PostMenu";
import ProfileMenu from "./ProfileMenu";

export default function WideHeader({ className }: { className: string }) {
  const { isLogin } = use(LoginMemberContext);

  return (
    <div className={className}>
      {!isLogin && (
        <a
          className="flex gap-2"
          href="http://localhost:8080/oauth2/authorization/kakao?redirectUrl=http://localhost:3000"
        >
          <MessageCircle />
          <p>카카오 로그인</p>
        </a>
      )}
      <HomeMenu />
      <PostMenu />
      {isLogin && <ProfileMenu />}
      <ModeToggle />
    </div>
  );
}
