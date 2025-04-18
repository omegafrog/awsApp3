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
          href={`${process.env.NEXT_PUBLIC_BACKEND_HOST}/oauth2/authorization/kakao?redirectUrl=${process.env.NEXT_PUBLIC_FRONTEND_HOST}`}
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
