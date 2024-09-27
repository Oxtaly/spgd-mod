/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Shattered Pixel Dungeon Mod
 * Copyright (C) 2024-2024 Oxtaly
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.NinePatch;

import java.util.function.Function;

public class UpdateableButton_MOD extends StyledButton {

    private Function<Button, String> contentGetter;
    private Function<Button, Chrome.Type> typeGetter;
    private String previousLabel;
    private Chrome.Type previousType;
    private final static String TEMP_LABEL_BC_I_AM_LAZY = "t3mp";

    public UpdateableButton_MOD(Function<Button, String> contentGetter ) {
        this( contentGetter, null );
    }
    public UpdateableButton_MOD(Function<Button, String> contentGetter, Function<Button, Chrome.Type> typeGetter ) {
        this( contentGetter, typeGetter, 9 );
    }

    public UpdateableButton_MOD(Function<Button, String> contentGetter, int size ) {
        this( Chrome.Type.RED_BUTTON, contentGetter, null, size );
    }
    
    public UpdateableButton_MOD(Function<Button, String> contentGetter, Function<Button, Chrome.Type> typeGetter, int size ) {
        this( Chrome.Type.RED_BUTTON, contentGetter, typeGetter, size );
    }

    public UpdateableButton_MOD(Chrome.Type type, Function<Button, String> contentGetter, Function<Button, Chrome.Type> typeGetter, int size ){
        super( type, TEMP_LABEL_BC_I_AM_LAZY, size);
        previousLabel = TEMP_LABEL_BC_I_AM_LAZY;
        previousType = type;
        this.contentGetter = contentGetter;
        this.typeGetter = typeGetter;
        update();
    }

    @Override
    public void update() {
        super.update();

        String newLabel = contentGetter.apply(this);
        Chrome.Type newType = typeGetter != null ? typeGetter.apply(this) : null;
        if(newLabel.equals(previousLabel) && (typeGetter == null | (newType != null && newType.equals(previousType)))) { // Same as previous label & type, no need to update
            return;
        }

        for (int i=0; i < length; i++) {
            Gizmo gizmo = members.get( i );
            if(typeGetter != null
                    && newType != null
                    && !newType.equals(previousType)
                    && gizmo != null
                    && gizmo instanceof NinePatch
            ) {
                remove(gizmo);
                i--;
                bg = Chrome.get( newType );
                addToBack( bg );
                previousType = newType;
                bg.update();
                if(newLabel.equals(previousLabel)) {
                    break;
                }
            }
            if (!newLabel.equals(previousLabel) && gizmo != null && gizmo instanceof RenderedTextBlock) {
                RenderedTextBlock textBlock = ((RenderedTextBlock) gizmo);
                if(textBlock.text().equals(previousLabel)) {
                    textBlock.text(newLabel);
                    textBlock.update();
                    previousLabel = newLabel;
                    break;
                }
            }
        }
    }
}
