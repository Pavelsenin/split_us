ALTER TABLE check_book
    ADD COLUMN invite_token VARCHAR(64);

CREATE UNIQUE INDEX ux_check_book_invite_token
    ON check_book(invite_token)
    WHERE invite_token IS NOT NULL;
