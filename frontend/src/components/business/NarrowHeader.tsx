import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
  DrawerTrigger,
} from "@/components/ui/drawer";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import {
  CircleHelp,
  House,
  Menu,
  MessageCircle,
  NotebookPen,
  NotebookText,
} from "lucide-react";
import Link from "next/link";
import { use } from "react";
import ProfileMenu from "./ProfileMenu";

export default function NarrowHeader({ className }: { className: string }) {
  const { isLogin } = use(LoginMemberContext);
  return (
    <div className={className}>
      <ProfileMenu />
      <Drawer>
        <DrawerTrigger>
          <Menu />
        </DrawerTrigger>
        <DrawerContent>
          <DrawerHeader>
            <DrawerTitle className="sr-only">menu</DrawerTitle>
            <DrawerDescription className="sr-only">menu</DrawerDescription>
          </DrawerHeader>
          <div>
            <ul className="flex flex-col gap-2 p-3">
              <li>
                <DrawerClose asChild>
                  {!isLogin && (
                    <a
                      className="flex gap-2"
                      href="http://localhost:8080/oauth2/authorization/kakao?redirectUrl=http://localhost:3000"
                    >
                      <MessageCircle />
                      <p>카카오 로그인</p>
                    </a>
                  )}
                </DrawerClose>
              </li>
              <li>
                <DrawerClose asChild>
                  <Link href="/" className="flex items-center gap-2">
                    <House />
                    <span>메인</span>
                  </Link>
                </DrawerClose>
              </li>
              <li>
                <DrawerClose asChild>
                  <Link href="/about" className="flex items-center gap-2">
                    <CircleHelp />
                    <span>소개</span>
                  </Link>
                </DrawerClose>
              </li>
              <hr />
              <li>
                <DrawerClose asChild>
                  <Link href="/post/write" className="flex items-center gap-2">
                    <NotebookPen />
                    <span>글 작성</span>
                  </Link>
                </DrawerClose>
              </li>
              <li>
                <DrawerClose asChild>
                  <Link href="/post/list" className="flex items-center gap-2">
                    <NotebookText />
                    <span>글 목록</span>
                  </Link>
                </DrawerClose>
              </li>
              {isLogin && (
                <li>
                  <DrawerClose asChild>
                    <Link
                      href="/post/list/me"
                      className="flex items-center gap-2"
                    >
                      <NotebookText />
                      <span>내 글</span>
                    </Link>
                  </DrawerClose>
                </li>
              )}
            </ul>
          </div>
        </DrawerContent>
      </Drawer>
    </div>
  );
}
